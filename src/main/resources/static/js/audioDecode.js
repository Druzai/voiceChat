const recording_blob_str = document.getElementById("recording-blob");
const recordedAudio = document.getElementById("recordedAudio");

fetch(recording_blob_str.innerText)
    .then(async res => {
        recordedAudio.src = URL.createObjectURL(await res.blob());
        recordedAudio.controls = true;
    })
