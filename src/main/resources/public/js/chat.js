/**
 * Created by bartek on 1/14/17.
 */
//Establish the WebSocket connection and set up event handlers
var webSocket = new WebSocket("ws://" + location.hostname + ":" + location.port + "/chat/");
webSocket.onmessage = function (msg) {
    updateChat(msg);
};
webSocket.onclose = function () {
    alert("WebSocket connection closed");
    location.href="/"
};



id("send").addEventListener("click", function () {
    sendMessage("/msg" + id("message").value);
});
id("leave").addEventListener("click", function () {
    sendMessage("/leave");
    id("chat").innerHTML = "";
    id("channel").style.display="none";
    id("channelselection").style.display="block";
});
id("message").addEventListener("keypress", function (e) {
    if (e.keyCode === 13) sendMessage("/msg" + e.target.value);
});
id("create").addEventListener("click", function () {
    if(id("newchannel").value!="") {
        webSocket.send("/create " + id("newchannel").value);
        id("newchannel").value = "";
    }
});
id("clear").addEventListener("click", function () {
    webSocket.send("/clear");
});
id("newchannel").addEventListener("keypress", function (e) {
    if (e.keyCode === 13 && id("newchannel").value!="") {
        webSocket.send("/create " + e.target.value);
        id("newchannel").value="";
    }
});


function sendMessage(message) {
    if (message !== "") {
        webSocket.send(message);
        id("message").value = "";
    }
}



function updateChat(msg) {
    var data = JSON.parse(msg.data);
    if (data.channel === "true") {
        insert("chat", data.userMessage);
        id("userlist").innerHTML = "";
        data.userlist.forEach(function (user) {
            insert("userlist", "<li>" + user + "</li>");

        })
    }
    else {
        id("channellist").innerHTML = "";
        data.channellist.forEach(function (channel) {
            insert("channellist", '<li><button id="' + channel + '" onclick="joinChannel(' + "'" + channel + "'" + ')">' + channel  + "</button></li>");
        });
    }
}

function joinChannel(chan) {
    webSocket.send("/join " + chan);
    id("channelselection").style.display="none";
    id("channel").style.display="block"
}

//Helper function for inserting HTML as the first child of an element
function insert(targetId, message) {
    id(targetId).insertAdjacentHTML("afterbegin", message);
}

//Helper function for selecting element by id
function id(id) {
    return document.getElementById(id);
}
