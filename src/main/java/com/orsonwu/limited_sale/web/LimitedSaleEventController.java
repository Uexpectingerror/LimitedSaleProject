package com.orsonwu.limited_sale.web;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.orsonwu.limited_sale.db.dao.LimitedSaleCommodityDao;
import com.orsonwu.limited_sale.db.dao.LimitedSaleEventDao;
import com.orsonwu.limited_sale.db.dao.LimitedSaleOrderDao;
import com.orsonwu.limited_sale.db.po.LimitedSaleCommodity;
import com.orsonwu.limited_sale.db.po.LimitedSaleEvent;
import com.orsonwu.limited_sale.db.po.LimitedSaleOrder;
import com.orsonwu.limited_sale.services.LimitedSaleEventService;
import com.orsonwu.limited_sale.util.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
public class LimitedSaleEventController {
    @Autowired
    private LimitedSaleEventDao limitedSaleEventDao;
    @Autowired
    private LimitedSaleCommodityDao limitedSaleCommodityDao;
    @Autowired
    private LimitedSaleEventService limitedSaleEventService;
    @Autowired
    private LimitedSaleOrderDao orderDao;
    @Autowired
    RedisService redisService;

    //map to the add event page
    @RequestMapping("/addLimitedSaleEvent")
    public String addLimitedSaleEvent() {
        return "add_event";
    }

    //map to the main events page
    @RequestMapping("/limitedSale")
    public String saleEventsPage (Map<String, Object> resultMap) {
        //Sentinel protection to limit query flow
        try(Entry entry = SphU.entry("limitedSaleEventsQuery")){
            //Try get all the events from redis first
            List<LimitedSaleEvent> saleEvents = limitedSaleEventService.getAllValidEventsFromRedis();
            //if not get it from the database
            if (saleEvents.isEmpty()){
                log.info("redis sale events info is empty, get them from the database");
                saleEvents = limitedSaleEventDao.queryLimitedSaleEventsByStatus(1);
                limitedSaleEventService.pushAllEventAndCommodityToRedis();
            }
            resultMap.put("limitedSaleEvents", saleEvents);
            return "limitedsale_event";
        }
        //return wait page if exceeds the flow limit
        catch (BlockException ex){
            log.error(ex.toString());
            return "wait";
        }
    }

    /**
     * Commodity page
     * @param resultMap
     * @param saleEventId
     * @return
     */
    @RequestMapping("/commodity/{saleEventId}")
    public String itemPage(Map<String,Object> resultMap,@PathVariable long saleEventId){
        //Sentinel protection to limit query flow
        try(Entry entry = SphU.entry("limitedSaleCommodityQuery")){

            //query the redis server for event and item info before accessing the database
            LimitedSaleEvent saleEvent = limitedSaleEventService.getValidEventFromRedis(saleEventId);
            if (saleEvent == null){
                log.info("target event not in redis");
                saleEvent = limitedSaleEventDao.queryLimitedSaleEventById(saleEventId);
                if (saleEvent == null){
                    return "error";
                }
                limitedSaleEventService.pushSaleEventInfoToRedis(saleEventId);
            }
            else
            {
                log.info("redis sale event info:" + saleEvent);
            }

            //Query redis first
            LimitedSaleCommodity saleCommodity = limitedSaleEventService.getValidCommodityFromRedis(saleEvent.getCommodityId());
            if (saleCommodity == null){
                log.info("target commodity not in redis");
                saleCommodity = limitedSaleCommodityDao.queryLimitedSaleCommodityById(saleEvent.getCommodityId());
                if (saleCommodity == null){
                    return "error";
                }
                limitedSaleEventService.pushSaleEventInfoToRedis(saleEventId);
            }else{
                log.info("redis sale commodity info:" + saleCommodity);
            }

            //Fill the event and item info into the result map
            resultMap.put("saleEvent",saleEvent);
            resultMap.put("saleCommodity",saleCommodity);
            resultMap.put("salePrice",saleEvent.getSalePrice());
            resultMap.put("originalPrice",saleEvent.getOriginalPrice());
            resultMap.put("commodityId",saleEvent.getCommodityId());
            resultMap.put("commodityName",saleCommodity.getCommodityName());
            resultMap.put("commodityDesc",saleCommodity.getCommodityDesc());
            return "limitedsale_commodity";

        }
        //return wait page if exceeds the flow limit
        catch (BlockException ex){
            log.error(ex.toString());
            return "wait";
        }
    }

    //map to the form submit page for add action
    @RequestMapping("/addLimitedSaleEventAction")
    public String addLimitedSaleEventAction(
            @RequestParam("eventName") String eventName,
            @RequestParam("commodityId") long commodityId,
            @RequestParam("salePrice") BigDecimal salePrice,
            @RequestParam("originalPrice") BigDecimal originalPrice,
            @RequestParam("totalStock") long totalStock,
            @RequestParam("startTime") String startTime,
            @RequestParam("endTime") String endTime,
            Map<String, Object> resultMap
    ) throws ParseException
    {
        startTime = startTime.substring(0, 10) + startTime.substring(11);
        endTime = endTime.substring(0, 10) + endTime.substring(11);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-ddhh:mm");

        LimitedSaleEvent saleEvent = new LimitedSaleEvent();
        saleEvent.setEventName(eventName);
        saleEvent.setCommodityId(commodityId);
        saleEvent.setSalePrice(salePrice);
        saleEvent.setOriginalPrice(originalPrice);
        saleEvent.setTotalStock(totalStock);
        saleEvent.setAvailableStock(totalStock);
        saleEvent.setLockStock(0L);
        saleEvent.setEventStatus(1);
        saleEvent.setStartTime(format.parse(startTime));
        saleEvent.setEndTime(format.parse(endTime));

        limitedSaleEventDao.insertLimitedSaleEvent(saleEvent);
        resultMap.put("limitedSaleEvent", saleEvent);
        return "add_success";
    }


    /**
     * Process order request
     * @param userId
     * @param limitedSaleEventId
     * @return
     */
    @ResponseBody
    @RequestMapping("/limitedSale/buy/{userId}/{limitedSaleEventId}")
    public ModelAndView buyLimitedCommodity(@PathVariable long userId, @PathVariable long limitedSaleEventId) {
        boolean stockValidateResult = false;

        ModelAndView modelAndView = new ModelAndView();
        try {
            /*
            * make sure the user is not restrained from participate this sale event
            * */
            if(limitedSaleEventService.isUserRestrainedFromEvent(limitedSaleEventId, userId))
            {
                //remind user that you exceed the sale limit
                modelAndView.addObject("resultInfo", "Sorry，you exceeded your purchasing limit...");
                modelAndView.setViewName("limitedsale_result");
                modelAndView.addObject("showOrderDetailButton", "display:none;");
                return modelAndView;
            }

            /*
             * make sure there is valid stock left (using lua inside redis)
             */
            stockValidateResult = limitedSaleEventService.limitedSaleStockValidator(limitedSaleEventId);
            if (stockValidateResult) {
                LimitedSaleOrder order = limitedSaleEventService.createOrder(limitedSaleEventId, userId);
                modelAndView.addObject("resultInfo","successfully secured the item，creating the order now，Order ID：" + order.getOrderNum());
                modelAndView.addObject("orderNo", order.getOrderNum());
            } else {
                modelAndView.addObject("resultInfo","Sorry，out of stock");
            }
        } catch (Exception e) {
            log.error("Unexpected Error" + e.toString());
            modelAndView.addObject("resultInfo","securing item failed");
        }
        modelAndView.setViewName("limitedsale_result");
        return modelAndView;
    }

    /**
     * Look up the order detail
     * @param orderNo
     * @return
     */
    @RequestMapping("/limitedSale/orderQuery/{orderNo}")
    public ModelAndView orderQuery(@PathVariable String orderNo) {
        log.info("Order Query，Oder Number：" + orderNo);
        ModelAndView modelAndView = new ModelAndView();

        if (orderNo.compareTo("null") == 0) {
            modelAndView.setViewName("order_null");
            return modelAndView;
        }

        LimitedSaleOrder order = orderDao.queryOrder(orderNo);
        if (order != null) {
            modelAndView.setViewName("order");
            modelAndView.addObject("order", order);
            LimitedSaleEvent saleEvent = limitedSaleEventDao.queryLimitedSaleEventById(order.getEventId());
            modelAndView.addObject("limitedSaleEvent", saleEvent);
        } else {
            modelAndView.setViewName("order_wait");
        }
        return modelAndView;
    }

/*    *//**
     * Pay for the order
     * @return
     */
    @RequestMapping("/limitedSale/payOrder/{orderNo}")
    public String payOrder(@PathVariable String orderNo) throws Exception {
        limitedSaleEventService.payOrderProcess(orderNo);
        return "redirect:/limitedSale/orderQuery/" + orderNo;
    }

    /**
     * Getting the current server time
     * @return
     */
    @ResponseBody
    @RequestMapping("/limitedSale/systemTime")
    public String getSystemTime() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//set data format
        String date = df.format(new Date());
        return date;
    }

    /**
     *  Define flow control rules
     *  @PostConstruct
     */
    @PostConstruct
    public void initLimitedSaleFlowControlRules(){
        //1.list to store all the rules
        List<FlowRule> rules = new ArrayList<>();
        //2.create a new rule
        FlowRule rule = new FlowRule();
        //set the resource that this rule protects
        rule.setResource("limitedSaleEventsQuery");
        //define control type, using QPS control
        rule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        //limit it to 1 for testing
        rule.setCount(100);

        //Second rule
        FlowRule rule2 = new FlowRule();
        rule2.setGrade(RuleConstant.FLOW_GRADE_QPS);
        rule2.setResource("limitedSaleCommodityQuery");
        rule2.setCount(100);


        rules.add(rule);
        rules.add(rule2);
        //4.load the rules
        FlowRuleManager.loadRules(rules);
    }
}
