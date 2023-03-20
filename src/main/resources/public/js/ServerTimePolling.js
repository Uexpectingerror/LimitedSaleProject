

const url='http://localhost:8080/limitedSale/systemTime';
var startSaleTime = new Date(document.getElementById("startTime").innerText)
var lastServerTime = new Date().getTime()
var remainTime = 0
var lastUpdateTime = new Date().getTime()
var cdUI = document.getElementById("timer")
function ServerTimeRequest(){
    const Http = new XMLHttpRequest();
    console.log("startTime: ", startSaleTime)
    Http.open("GET", url);
    Http.send();
    Http.onreadystatechange = function() {
        if (this.readyState == 4 && this.status == 200){
            lastServerTime = new Date(Http.responseText)
            remainTime = startSaleTime.getTime() - lastServerTime.getTime()
            return true
        }
    }
}

function updateCountDown() {
    setTimeout(function () {
        remainTime = remainTime - (new Date().getTime() - lastUpdateTime);
        let diff = remainTime
        const hours = Math.floor(diff / 1000 / 60 / 60);
        diff -= hours * 1000 * 60 * 60;
        const minutes = Math.floor(diff / 1000 / 60);
        diff -= minutes * 1000 * 60;
        const secs = Math.floor(diff / 1000);
        cdUI.innerHTML = "Time remains " + hours + ":" + minutes+ ":"+ secs
        lastUpdateTime = new Date().getTime()
        updateCountDown();
    }, 1000);
}

function polling() {
    ServerTimeRequest(url, startSaleTime)
    setTimeout(function(){
        polling()
    }, 10000)
}

polling()
updateCountDown()
