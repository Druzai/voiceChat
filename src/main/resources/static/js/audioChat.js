const userId = parseInt(document.getElementById("user_id").innerText);
const userName = document.getElementById("user_name").innerText;
const roomId = parseInt(document.getElementById("room_id").innerText);
const usersDiv = document.getElementById("users");
const userLogDiv = document.getElementById("messages");
const connectButton = document.getElementById("connect_button");
const muteButton = document.getElementById("mute_button");
const postButton = document.getElementById("postButton");
const textAreaBlock = document.getElementById("textAreaBlock");
let stompClient = null;
let recordingState = false;
let muteState = true;
let users = [];

if (!navigator.getUserMedia)
    navigator.getUserMedia = navigator.webkitGetUserMedia || navigator.mozGetUserMedia;

function addLog(msg) {
    let innerDiv = document.createElement('div');
    innerDiv.innerText = msg;
    userLogDiv.appendChild(innerDiv);
    scrollBottom();
}

const $chat = $('.chat'),
    $printer = $('.messages', $chat),
    printerH = $printer.innerHeight();

function scrollBottom() {
    $printer.stop().animate({scrollTop: $printer[0].scrollHeight - printerH}, 600);
}

function connect() {
    let socket = new SockJS('/message-ws');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        console.log('Connected: ' + frame);
        stompClient.send("/app/send", {}, JSON.stringify({
            'userId': userId,
            'userName': userName,
            'roomId': roomId,
            'command': "connect",
            'content': "",
            'userList': []
        }));
        stompClient.subscribe('/topic/messages', function (message) {
            let response = JSON.parse(message.body)
            if (response.roomId === roomId) {
                if (response.command === "connect") {
                    users = response.userList;
                    usersDiv.innerText = users.join(", ");
                    addLog(`${response.userName} подключился!`);
                } else if (response.command === "disconnect") {
                    users = response.userList;
                    usersDiv.innerText = users.join(", ");
                    addLog(`${response.userName} отключился!`);
                } else if (response.command === "micOff") {
                    addLog(`${response.userName} выключил микрофон!`);
                } else if (response.command === "micOn") {
                    addLog(`${response.userName} включил микрофон!`);
                } else if (response.command === "message") {
                    addLog(`<${response.userName}>: ${response.content}`)
                }
                if (response.userId !== userId && response.command === "voice") {
                    const audio = new Audio(response.content);
                    audio.play(response.content);
                }
            }
        });
    });
}

function disconnect() {
    if (stompClient !== null) {
        stompClient.send("/app/send", {}, JSON.stringify({
            'userId': userId,
            'userName': userName,
            'roomId': roomId,
            'command': "disconnect",
            'content': "",
            'userList': []
        }));
        usersDiv.innerText = "";
        users = [];
        stompClient.disconnect();
    }
}

connectButton.onclick = () => {
    if (!recordingState) {
        console.log("Recording started")
        recordingState = true;
        connectButton.innerText = "Отключиться";
        connect();
    } else {
        console.log("Recording stopped")
        recordingState = false;
        connectButton.innerText = "Подключиться";
        disconnect();
    }
}

muteButton.onclick = () => {
    if (!muteState) {
        console.log("Muted mic!")
        muteState = true;
        muteButton.innerText = "Включить микрофон"
        if (stompClient !== null)
            stompClient.send("/app/send", {}, JSON.stringify({
                'userId': userId,
                'userName': userName,
                'roomId': roomId,
                'command': "micOff",
                'content': "",
                'userList': []
            }));
    } else {
        console.log("Unmuted mic!")
        muteState = false;
        muteButton.innerText = "Выключить микрофон"
        if (stompClient !== null)
            stompClient.send("/app/send", {}, JSON.stringify({
                'userId': userId,
                'userName': userName,
                'roomId': roomId,
                'command': "micOn",
                'content': "",
                'userList': []
            }));
    }
}

postButton.onclick = () => {
    textAreaBlock.value = textAreaBlock.value.replace(/^\n/gm, "")
    if (textAreaBlock.value !== "" && stompClient !== null) {
        stompClient.send("/app/send", {}, JSON.stringify({
            'userId': userId,
            'userName': userName,
            'roomId': roomId,
            'command': "message",
            'content': textAreaBlock.value,
            'userList': []
        }));
        textAreaBlock.value = ""
    }
}

$(document).ready(function () {
    $('#textAreaBlock').keypress(function (e) {
        if (e.keyCode === 13)
            $('#postButton').click();
    });
});

window.onload = (e) => {
    mainFunction(500); // 1000
};

window.addEventListener('beforeunload', function (e) {
    disconnect();
});


function mainFunction(time) {
    navigator.mediaDevices.getUserMedia({audio: true, video: false}).then((stream) => {
        const mediaRecorder = new MediaRecorder(stream);
        mediaRecorder.start();
        let audioChunks = [];

        mediaRecorder.addEventListener("dataavailable", function (event) {
            audioChunks.push(event.data);
        });

        mediaRecorder.addEventListener("stop", function () {
            if (stompClient !== null && recordingState) {
                const audioBlob = new Blob(audioChunks);
                audioChunks = [];

                const fileReader = new FileReader();
                fileReader.readAsDataURL(audioBlob);
                fileReader.onloadend = function () {
                    if (muteState)
                        return;
                    const base64String = fileReader.result;
                    stompClient.send("/app/send", {}, JSON.stringify({
                        'userId': userId,
                        'userName': userName,
                        'roomId': roomId,
                        'command': "voice",
                        'content': base64String,
                        'userList': []
                    }));
                };
            }
            mediaRecorder.start();

            setTimeout(function () {
                mediaRecorder.stop();
            }, time);
        });

        setTimeout(function () {
            mediaRecorder.stop();
        }, time);
    });
}