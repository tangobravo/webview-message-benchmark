(function() {
    var data = []
    var lastNow = 0;
    var receivedMessage = 0;

    function messageHandler(event) {
        if(event.data.length <= 5) {
            if(event.data === "start") {
                data = [];
                lastNow = performance.now();
            }
            if(event.data === "end") {
                outputResults();
            }
            return;
        }

        time = performance.now() - lastNow;
        data.push([event.data.length, time]);
        receivedMessage = event.data;
        lastNow = performance.now();
    }

    function strToUint16Array(str, av) {
        for(var i = 0; i < str.length; ++i)
            av[i] = str.charCodeAt(i);
    }

    function outputResults() {
        var ab = new ArrayBuffer(receivedMessage.length * 2);
        var av = new Uint16Array(ab);
        var startConversion = performance.now();
        strToUint16Array(receivedMessage, av);
        var conversionTime = performance.now() - startConversion;

        var startCopy = performance.now();
        var abCopy = ab.slice(0);
        var copyTime = performance.now() - startCopy;

        var resultText = "";
        for(var i = 0; i < data.length; ++i) {
            resultText += data[i][0].toString() + " characters, " + data[i][1].toFixed(2) + " ms\n";
        }

        resultText += "\nString to ArrayBuffer: " + conversionTime.toFixed(2) + " ms\n";
        resultText += "\nArrayBuffer copy: " + copyTime.toFixed(2) + " ms\n";

        resultText += "\nFirst 10 code units:\n";
        for(var i = 0; i < 10; ++i) {
            resultText += i.toString() + ": 0x" + receivedMessage.charCodeAt(i).toString(16) + " 0x" + av[i].toString(16) + "\n";
        }
        resultText += "\n";

        document.getElementById("results").innerText = resultText;
    }

    window.addEventListener("message", messageHandler, false);
})();
