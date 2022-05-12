const record = document.getElementById("record");
let recordingState = false;
const submitButton = document.getElementById("submit-button");
const recordingContent = document.getElementById("recording-content");
const recordedAudio = document.getElementById("recordedAudio");
let audioChunks = [];
let rec;

if (!navigator.getUserMedia)
    navigator.getUserMedia = navigator.webkitGetUserMedia || navigator.mozGetUserMedia;

navigator.mediaDevices.getUserMedia({audio: true, video: false})
    .then(stream => {
        handlerFunction(stream)
    })


function handlerFunction(stream) {
    rec = new MediaRecorder(stream);
    rec.ondataavailable = e => {
        audioChunks.push(e.data);
        console.log(e.data);
        if (rec.state === "inactive") {
            let blob = new Blob(audioChunks, {type: 'audio/mpeg-3'});
            recordedAudio.src = URL.createObjectURL(blob);
            recordedAudio.controls = true;
            getData(blob, sendData);
        }
    }
}

function sendData(data) {
    recordingContent.value = data;
}

function getData(audioFile, callback) {
    const reader = new FileReader();
    reader.readAsDataURL(audioFile);
    reader.onloadend = function () {
        var base64String = reader.result;
        callback(base64String);
        console.log('Base64 String - ', base64String);
    }
}

record.onclick = () => {
    if (!recordingState) {
        console.log("Recording started")
        recordingState = true;
        record.style.backgroundColor = "red"
        audioChunks = [];
        rec.start();
    } else {
        console.log("Recording stopped")
        recordingState = false;
        stop.disabled = true;
        submitButton.disabled = false;
        record.style.backgroundColor = "green"
        rec.stop();
    }
}