(function() {
    var data = []
    var lastNow = 0;
    var receivedMessage = 0;
    var postMessageActive = false;

    var nativeMessagePort = 0;
    var channelTestLoop = false;
    var mainFrameTestLoop = false;
    var bytesSinceReset = 0;
    var resetTime = 0;

    function updateThroughputStats(bytesReceived) {
        bytesSinceReset += bytesReceived;
        let msSinceReset = performance.now() - resetTime;
        if(msSinceReset > 1000) {
            mbSinceReset = bytesSinceReset / 1000000;
            secsSinceReset = msSinceReset / 1000;
            throughput = mbSinceReset / secsSinceReset;
            document.getElementById("results").innerText = `Throughput ${throughput.toFixed(1)} MB/sec`;
            bytesSinceReset = 0;
            resetTime = performance.now();
        }
    }

    function messageFromPort(event) {
        console.log("received message from port, length " + event.data.length);
        updateThroughputStats(event.data.length * 2); // 2 bytes per character
        if(channelTestLoop) nativeMessagePort.postMessage("app://data?len=1000000");
    }

    function setNativeMessagePort(port) {
        nativeMessagePort = port;
        nativeMessagePort.onmessage = messageFromPort;
        document.getElementById("channelDiv").style.display = "block";
        document.getElementById("startChannelTest").onclick = startChannelTest;
        document.getElementById("startMainFrameTest").onclick = startMainFrameTest;
        document.getElementById("stopTests").onclick = stopTests;
    }

    function startChannelTest(e) {
        e.preventDefault();
        if(!channelTestLoop) {
            channelTestLoop = true;
            mainFrameTestLoop = false;
            bytesSinceReset = 0;
            resetTime = performance.now();
            nativeMessagePort.postMessage("app://data?len=1000000");
        }
    }

    function startMainFrameTest(e) {
        e.preventDefault();
        if(!mainFrameTestLoop) {
            channelTestLoop = false;
            mainFrameTestLoop = true;
            bytesSinceReset = 0;
            resetTime = performance.now();
            nativeMessagePort.postMessage("app://data?len=1000000&mainFrame");
        }
    }

    function stopTests(e) {
        e.preventDefault();
        channelTestLoop = false;
        mainFrameTestLoop = false;
    }

    function messageHandler(event) {
        if(event.data.length <= 6) {
            if(event.data === "start") {
                data = [];
                lastNow = performance.now();
                postMessageActive = true;
            }
            if(event.data === "end") {
                outputResults();
                postMessageActive = false;
            }
            if(event.data == "msgchn") {
                setNativeMessagePort(event.ports[0]);
            }
            return;
        }

        if(postMessageActive) {
            time = performance.now() - lastNow;
            data.push([event.data.length, time]);
            receivedMessage = event.data;
            lastNow = performance.now();
            return;
        }

        console.log("received message on main frame, length " + event.data.length);
        updateThroughputStats(event.data.length * 2); // 2 bytes per character
        if(mainFrameTestLoop) nativeMessagePort.postMessage("app://data?len=1000000&mainFrame");
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
