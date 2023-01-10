"use strict";

///////////////////////////////////////////////
//Callback Functions:

function handleMessageCB(event) {
    // console.log("Message received: " + event.data)

    let serverResponse = JSON.parse(event.data)

    if (serverResponse.cmd == "join" || serverResponse.cmd == "leave") {

        let jsonUser = serverResponse.username
        let roomName = serverResponse.roomname
        let listOfNames = serverResponse.userlist

        // console.log("CMD: " + serverResponse.cmd)

        if (serverResponse.username === user.userName.value) {

            if (serverResponse.cmd === "join") {

                let title = document.getElementById("title")
                title.innerHTML = '<h1>' + roomName + '</h1>'

                let main = document.getElementById("main")
                main.innerHTML =
                    '<div class="container" id="container">' +
                    // '   <div class="flex chat-container" id="chat-container">' +
                    '       <div class="flex top" id="top">' +
                    '           <div class="members-btn" id="members-btn">' +
                    '               <p>Users In Room <i class="fa fa-chevron-down"></i></p>' +
                    '               <div class="dropdown-content" id="chat-members"></div>' +
                    '           </div>' +
                    '           <div class="buttons">' +
                    '               <p class="btn" id="btn-leave">Leave</p>' +
                    '           </div>' +
                    '        </div>' +
                    '       <div class="chat-box" id="chat-box">' +
                    '       </div>' +
                    // '   </div>' +
                    '   <div class="message-container">' +
                    '       <p>Message:</p>' +
                    '       <div class="flex message">' +
                    '           <textarea name="message" id="message" rows="1"></textarea>' +
                    '           <div class="buttons">' +
                    '               <p class="btn" id="btn-send">Send</p>' +
                    '           </div>' +
                    '       </div>' +
                    '   </div>' +
                    '</div>'

                // let chatBox = document.getElementById('chat-box')
                // let msgDiv = document.createElement('div')
                // chatBox.appendChild(msgDiv)
                // msgDiv.classList.add("user-update")
                //
                // let updateP = document.createElement('p')
                // msgDiv.appendChild(updateP)
                // let text = document.createTextNode(jsonUser + " has entered the room")
                // updateP.appendChild(text)


                let message = document.getElementById('message')
                let send = document.getElementById('btn-send')
                send.addEventListener("click", handleSendMessage)
                message.addEventListener("keypress", handleSendMessage)

                let leave = document.getElementById("btn-leave")
                leave.addEventListener("click", handleLeave)

                let dropdownBtn = document.getElementById("members-btn")
                dropdownBtn.addEventListener("click", handleDropdown)
                // window.addEventListener("click", handleWindowClick)


            } else if (serverResponse.cmd === "leave") {

                let title = document.getElementById("title")
                title.innerHTML = '<h1>Join a Chat Room</h1>'

                let main = document.getElementById("main")
                main.innerHTML =
                    '<div class="flex room-info" id="room-info">' +
                    '   <div class="room-info__user">' +
                    '       <label for="username">User Name:</label>' +
                    '       <input type="text" id="username" name="username" size="10">' +
                    '   </div>' +
                    '   <div class="room-info__room">' +
                    '       <label htmlFor="roomname">Room Name:</label>' +
                    '       <input type="text" id="roomname" name="roomname" size="10">' +
                    '   </div>' +
                    '   <p class="btn" id="btn-join">Enter Room</p>' +
                    '</div>'
                let tempName = user.userName.value
                user.userName = document.getElementById("username")
                user.userName.value = tempName

                let tempRoom = user.roomName.value
                user.roomName = document.getElementById("roomname")
                user.roomName.value = tempRoom

                let join = document.getElementById("btn-join")
                join.addEventListener("click", handleJoin)
                user.userName.addEventListener("keypress", handleJoin)
                user.roomName.addEventListener("keypress", handleJoin)
            }
        }

        let names = [];
        if (listOfNames == user.userName.value) {
            names.push(listOfNames)
        } else {
            names = listOfNames.split(",");
        }

        // add code to change html/css
        let chatMembers = document.getElementById('chat-members')
        chatMembers.innerHTML = ""
        for (let name of names) {
            let memberStr = "<p>" + name + "</p>"
            chatMembers.innerHTML += memberStr
        }

    } else if (serverResponse.cmd === "msg") {

        let jsonUser = serverResponse.username
        let jsonMessage = serverResponse.message
        let jsonTimestamp = parseFloat(serverResponse.timestamp)

        let dateString = new Date(jsonTimestamp).toLocaleDateString()
        let timeString = new Date(jsonTimestamp).toLocaleTimeString()

        let userName = document.createTextNode(jsonUser + ": ")
        let newMessage = document.createTextNode(jsonMessage)
        let timestamp = document.createTextNode(dateString + " " + timeString)


        let chatBox = document.getElementById('chat-box')
        let msgDiv = document.createElement('div')
        chatBox.appendChild(msgDiv)
        msgDiv.classList.add("msg")


        let avatarDiv = document.createElement('div')
        msgDiv.appendChild(avatarDiv)
        avatarDiv.classList.add("avatar")

        let avatarText = document.createElement('p')
        avatarDiv.appendChild(avatarText)
        avatarText.classList.add("avatar-text")
        let firstLetter = document.createTextNode(jsonUser.substr(0, 1))
        avatarText.appendChild(firstLetter)

        let messageDiv = document.createElement('div')
        msgDiv.appendChild(messageDiv)
        messageDiv.classList.add("message")

        let usernameP = document.createElement('p')
        messageDiv.appendChild(usernameP)
        usernameP.classList.add("username")
        usernameP.appendChild(userName)

        let messageTextP = document.createElement('p')
        messageDiv.appendChild(messageTextP)
        messageTextP.classList.add("message-text")
        messageTextP.appendChild(newMessage)

        let timestampP = document.createElement('p')
        chatBox.appendChild(timestampP)
        timestampP.classList.add("timestamp")
        timestampP.appendChild(timestamp)

        if (jsonUser == user.userName.value) {
            msgDiv.classList.add("msg-right")
            timestampP.classList.add("timestamp-right")
        } else {
            msgDiv.classList.add("msg-left")
            timestampP.classList.add("timestamp-left")
        }

        msgDiv.scrollIntoView({behavior: "smooth"})

    } else {
        console.log("Invalid Message from Server")
        alert("Invalid Message from Server")
    }

}

function handleOpenCB() {
    console.log("Server connected");
}

function handleCloseCB() {
    console.log("Server closed");
}

function handleWSErrorCB() {
    console.log("A WebSocket error occurred");
}

///////////////////
// Joining Room

function validateUserName(userName) {
    // console.log(userName.value)
    if (userName.value == "") {
        return false
    } else if (userName.value == "join") {
        console.log("Forbidden: Username cannot be \"join\"")
        return false
    } else if (userName.value == "leave") {
        console.log("Forbidden: Username cannot be \"leave\"")
        return false
    }

    for (let userFromArray of users) {
        if (userName.value == userFromArray.userName.value) {
            console.log("duplicate name")
            return false
        }
    }
    return true
}

function validateRoomName(roomName) {
    // console.log(roomName.value)
    if (roomName.value == "") {
        return false
    }
    for (let i = 0; i < roomName.value.length; i++) {
        let char = roomName.value[i]
        if (char < 'a' || char > 'z') {
            return false
        }
    }
    return true
}

function handleJoin(event) {

    if (event.type == "click" || event.keyCode == 13) {
        event.preventDefault()

        if (validateUserName(user.userName)) {
            if (validateRoomName(user.roomName)) {

                ws.send("join " + user.roomName.value + " " + user.userName.value)

                // ws.send(userName.value + " has joined room: " + roomName.value)
            } else {
                console.log("Invalid Room Name")
            }
        } else {
            console.log("Invalid Username")
        }
    }
}

function handleLeave() {
    ws.send("leave")
}

function handleSendMessage(event) {
    if (event.type == "click" || event.keyCode == 13) {
        event.preventDefault()

        let datestamp = Date.now()
        // console.log("Datestamp: " + datestamp)
        let dateString = new Date(datestamp).toLocaleDateString()
        let timeString = new Date(datestamp).toLocaleTimeString()
        // console.log("Date/Time string: " + dateString + " " + timeString)

        if (message.value != "") {
            ws.send("msg " + datestamp + " " + message.value)
        }
        // console.log("Sending: " + user.userName.value + " " + message.value)
        message.value = ""
    }
}

function handleDropdown(event){
    // console.log("in handleDropdown")
    document.getElementById("chat-members").classList.toggle("show-dropdown");
}

// function handleWindowClick(event){
//     if (!event.target.matches('.members-btn')) {
//         let dropdown = document.getElementById("chat-members");
//         if (dropdown.classList.contains('show-dropdown')) {
//             dropdown.classList.remove('show-dropdown');
//         }
//     }
// }


/////////////////////////
// Declare variables
let userName
let roomName
let user = {userName, roomName}
user.userName = document.getElementById('username')
user.roomName = document.getElementById('roomname')
let join = document.getElementById("btn-join")

let users = new Array()

let inRoom = false
let leftRoom = false

join.addEventListener("click", handleJoin)
user.userName.addEventListener("keypress", handleJoin)
user.roomName.addEventListener("keypress", handleJoin)


// let message = document.getElementById('message')
// let send = document.getElementById('btn-send')
// send.addEventListener("click", handleSendMessage)
// message.addEventListener("keypress", handleSendMessage)

// let leave = document.getElementById("btn-leave")
// leave.addEventListener("click", handleLeave)

// console.log("make web socket");
let ws = new WebSocket("ws://localhost:8080");
ws.onmessage = handleMessageCB;
ws.onopen = handleOpenCB;
ws.onclose = handleCloseCB;
ws.onerror = handleWSErrorCB;