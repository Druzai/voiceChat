const userId = parseInt(document.getElementById("user_id").innerText);
// const userName = document.getElementById("user_name").innerText;
const roomId = parseInt(document.getElementById("room_id").innerText);
const usersDiv = document.getElementById("users");
const connectButton = document.getElementById("connect_button");
const muteButton = document.getElementById("mute_button");
let stompClient = null;
let recordingState = false;
let muteState = false;
let users = [];

if (!navigator.getUserMedia)
    navigator.getUserMedia = navigator.webkitGetUserMedia || navigator.mozGetUserMedia;

function connect() {
    let socket = new SockJS('/message-ws');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        console.log('Connected: ' + frame);
        stompClient.send("/app/send", {}, JSON.stringify({
            'userId': userId,
            'roomId': roomId,
            'command': "connect",
            'audioContent': "",
            'userList': []
        }));
        stompClient.subscribe('/topic/messages', function (message) {
            let response = JSON.parse(message.body)
            // console.log(response);
            if (response.roomId === roomId) {
                if (response.command === "connect")
                    users = response.userList;
                    // users.push(response.userName);
                else if (response.command === "disconnect")
                    users = response.userList;
                    // users = users.filter(u => u !== response.userName);
                // usersDiv.innerHTML = '';
                // for (const name in users) {
                //     const li = document.createElement("li");
                //     li.innerText = name;
                //     usersDiv.append(li);
                // }
                usersDiv.innerText = users.join(", ");
                if (response.userId !== userId && response.command === "voice") {
                    const audio = new Audio(response.audioContent);
                    audio.play(response.audioContent);
                }
            }
        });
    });
}

function disconnect() {
    if (stompClient !== null) {
        stompClient.send("/app/send", {}, JSON.stringify({
            'userId': userId,
            'roomId': roomId,
            'command': "disconnect",
            'audioContent': "",
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
    if (!muteState){
        console.log("Muted mic!")
        muteState = true;
        muteButton.innerText = "Включить микрофон"
    }
    else {
        console.log("Unmuted mic!")
        muteState = false;
        muteButton.innerText = "Выключить микрофон"
    }
}


window.onload = (e) => {
    mainFunction(500); // 1000
};


function mainFunction(time) {
    navigator.mediaDevices.getUserMedia({audio: true}).then((stream) => {
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
                    // if (!userStatus.microphone || !userStatus.online) return;
                    const base64String = fileReader.result;
                    stompClient.send("/app/send", {}, JSON.stringify({
                        'userId': userId,
                        'roomId': roomId,
                        'command': "voice",
                        'audioContent': base64String,
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

//
// usernameLabel.onclick = function () {
//     usernameDiv.style.display = "block";
//     usernameLabel.style.display = "none";
// }
//
// function changeUsername() {
//     userStatus.username = usernameInput.value;
//     usernameLabel.innerText = userStatus.username;
//     usernameDiv.style.display = "none";
//     usernameLabel.style.display = "block";
//     emitUserInformation();
// }
//
// function toggleConnection(e) {
//     userStatus.online = !userStatus.online;
//
//     editButtonClass(e, userStatus.online);
//     emitUserInformation();
// }
//
// function toggleMute(e) {
//     userStatus.mute = !userStatus.mute;
//
//     editButtonClass(e, userStatus.mute);
//     emitUserInformation();
// }
//
// function toggleMicrophone(e) {
//     userStatus.microphone = !userStatus.microphone;
//     editButtonClass(e, userStatus.microphone);
//     emitUserInformation();
// }
//
//
// function editButtonClass(target, bool) {
//     const classList = target.classList;
//     classList.remove("enable-btn");
//     classList.remove("disable-btn");
//
//     if (bool)
//         return classList.add("enable-btn");
//
//     classList.add("disable-btn");
// }
//
// function emitUserInformation() {
//     socket.emit("userInformation", userStatus);
// }