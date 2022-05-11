// const b64toBlob = (b64Data, contentType="", sliceSize=512) => {
//     const byteCharacters = atob(b64Data);
//     const byteArrays = [];
//
//     for (let offset = 0; offset < byteCharacters.length; offset += sliceSize) {
//         const slice = byteCharacters.slice(offset, offset + sliceSize);
//
//         const byteNumbers = new Array(slice.length);
//         for (let i = 0; i < slice.length; i++) {
//             byteNumbers[i] = slice.charCodeAt(i);
//         }
//
//         const byteArray = new Uint8Array(byteNumbers);
//         byteArrays.push(byteArray);
//     }
//
//     return new Blob(byteArrays, {type: contentType});
// }

const recording_blob_str = document.getElementById("recording-blob");
const recordedAudio = document.getElementById("recordedAudio");

fetch(recording_blob_str.innerText)
    .then(async res => {
        recordedAudio.src = URL.createObjectURL(await res.blob());
        recordedAudio.controls = true;
    })
