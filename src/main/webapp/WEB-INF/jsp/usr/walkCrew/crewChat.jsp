<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<html>
<head>
  <title>산책 크루 채팅</title>
  <script src="/webjars/sockjs-client/1.5.1/sockjs.min.js"></script>
  <script src="/webjars/stomp-websocket/2.3.4/stomp.min.js"></script>
  <style>
    body {
      font-family: 'SUIT', sans-serif;
      background: linear-gradient(to bottom, #DBE3A3, #FEE191);
      margin: 0; padding: 20px;
    }

    #chatBox {
      width: 90%; max-width: 500px; height: 500px;
      margin: auto; padding: 20px;
      background: #ffffffcc;
      border-radius: 30px;
      overflow-y: auto;
      box-shadow: 0 4px 12px rgba(0,0,0,0.1);
    }

    .msg { display: flex; margin: 12px 0; align-items: flex-end; }
    .msg.me { justify-content: flex-end; }

    .profile {
      width: 36px; height: 36px; border-radius: 50%;
      margin-right: 8px; object-fit: cover;
      border: 2px solid #eee4c1;
    }

    .profile-placeholder { width: 36px; height: 36px; margin-right: 8px; }

    .bubble-wrap { max-width: 70%; }
    .bubble {
      padding: 10px; background-color: #fff9d4;
      border-radius: 18px; box-shadow: 0 2px 6px rgba(0,0,0,0.1);
      white-space: pre-wrap;
    }

    .msg.me .bubble { background-color: #d0f4e1; }

    .nickname {
      font-size: 13px; font-weight: bold;
      margin-bottom: 4px; color: #585858;
    }

    .time {
      font-size: 11px; color: #aaa;
      text-align: right; margin-top: 4px;
    }

    .date-divider {
      text-align: center; color: #888;
      margin: 20px 0 10px; font-size: 13px;
    }

    #chatInput {
      width: 80%; max-width: 400px;
      padding: 10px; border-radius: 20px;
      border: none; outline: none;
      box-shadow: 0 2px 5px rgba(0,0,0,0.1);
    }

    #sendBtn {
      padding: 10px 16px;
      background-color: #A7CFB3;
      color: white; border: none;
      border-radius: 20px;
      cursor: pointer; margin-left: 8px;
    }
  </style>
</head>
<body>

<h2 style="text-align:center; margin-bottom:20px;">🗨️ 산책 크루 채팅</h2>
<div id="chatBox"></div>

<div style="text-align:center; margin-top:20px;">
  <input type="text" id="chatInput" placeholder="메시지를 입력하세요">
  <button id="sendBtn">전송</button>
</div>

<c:set var="nickname" value="${loginedMember.nickname}" />
<c:set var="photo" value="${pet.photo}" />
<script>
  const crewId = ${crewId};
  const senderId = ${loginedMember.id};
  const senderNickname = "${fn:escapeXml(nickname)}";
  const senderPhoto = "${fn:escapeXml(photo)}";

  let stompClient = null;
  let groupBuffer = [];
  let lastDate = "";
  let lastGroupKey = "";
  let flushTimeout = null;
  let isSending = false;
  let enterPressed = false;
  let isComposing = false;

  function connect() {
    const socket = new SockJS("/ws");
    stompClient = Stomp.over(socket);

    stompClient.connect({}, function () {
      stompClient.subscribe("/topic/crew/" + crewId, function (msg) {
        const message = JSON.parse(msg.body);
        renderMessage(message);
      });
    });
  }

  function sendMessage() {
    if (isSending) return;

    const input = document.getElementById("chatInput");
    const content = input.value.trim();
    if (!content) return;

    const message = {
      crewId: crewId,
      senderId: senderId,
      nickname: senderNickname,
      content: content,
      photo: senderPhoto
    };

    stompClient.send("/app/chat.send/" + crewId, {}, JSON.stringify(message));
    input.value = "";
    isSending = true;
    setTimeout(function () { isSending = false; }, 200);
  }

  // 메세지 불러오기
  function renderMessage(msg) {
    const timeKey = msg.sentAt.slice(0, 16); // 분 단위
    const groupKey = msg.senderId + "_" + timeKey;

    if (groupKey !== lastGroupKey && groupBuffer.length > 0) {
      renderGroup(groupBuffer);
      groupBuffer = [];
    }

    groupBuffer.push(msg);
    lastGroupKey = groupKey;

    clearTimeout(flushTimeout);
    flushTimeout = setTimeout(() => {
      if (groupBuffer.length > 0) {
        renderGroup(groupBuffer);
        groupBuffer = [];
      }
    }, 300);
  }



  function renderGroup(messages) {
    if (!messages.length) return;

    const lastMsg = messages[messages.length - 1];
    const msgDate = formatDateOnly(lastMsg.sentAt);

    if (lastDate !== msgDate) {
      const divider = document.createElement("div");
      divider.className = "date-divider";
      divider.textContent = msgDate;
      document.getElementById("chatBox").appendChild(divider);
      lastDate = msgDate;
    }

    const currentTimeKey = formatTime(messages[0].sentAt);

    const allTimeElements = document.querySelectorAll(".msg .time");
    allTimeElements.forEach(function (el) {
      if (el.textContent === currentTimeKey) {
        el.remove();
      }
    });


    const firstMsg = messages[0];
    const isMe = firstMsg.senderId === senderId;
    const chatBox = document.getElementById("chatBox");

    messages.forEach(function (msg, index) {
      const isFirst = index === 0;
      const isLast = index === messages.length - 1;

      const msgDiv = document.createElement("div");
      msgDiv.className = "msg" + (isMe ? " me" : "");

      if (!isMe) {
        const profileHtml = isFirst
                ? "<img class='profile' src='" + (msg.photo || "/img/default-pet.png") + "' />"
                : "<div class='profile-placeholder'></div>";
        msgDiv.innerHTML += profileHtml;
      }

      const wrap = document.createElement("div");
      wrap.className = "bubble-wrap";

      if (!isMe && isFirst) {
        const nick = document.createElement("div");
        nick.className = "nickname";
        nick.textContent = msg.nickname || "알 수 없음";
        wrap.appendChild(nick);
      }

      const bubble = document.createElement("div");
      bubble.className = "bubble";
      bubble.textContent = msg.content;
      wrap.appendChild(bubble);

      if (isLast) {
        const time = document.createElement("div");
        time.className = "time";
        time.textContent = formatTime(msg.sentAt);
        wrap.appendChild(time);
      }

      msgDiv.appendChild(wrap);
      chatBox.appendChild(msgDiv);
    });

    requestAnimationFrame(scrollToBottom);
  }





  function formatDateOnly(iso) {
    const d = new Date(iso);
    return d.getFullYear() + "." + String(d.getMonth() + 1).padStart(2, "0") + "." + String(d.getDate()).padStart(2, "0");
  }

  function formatTime(iso) {
    const d = new Date(iso);
    return String(d.getHours()).padStart(2, "0") + ":" + String(d.getMinutes()).padStart(2, "0");
  }

  function scrollToBottom() {
    const chatBox = document.getElementById("chatBox");
    chatBox.scrollTop = chatBox.scrollHeight;
  }

  function loadPreviousMessages() {
    fetch("/usr/walkCrew/chat/api/" + crewId + "/messages")
            .then(function (res) { return res.json(); })
            .then(function (data) {
              data.forEach(renderMessage);
              renderGroup(groupBuffer);
              groupBuffer = [];
              scrollToBottom();
            });
  }

  document.addEventListener("DOMContentLoaded", function () {
    const input = document.getElementById("chatInput");

    input.addEventListener("compositionstart", function () { isComposing = true; });
    input.addEventListener("compositionend", function () { isComposing = false; });

    input.addEventListener("keydown", function (e) {
      if (e.key === "Enter" && !e.shiftKey && !isComposing) {
        e.preventDefault();
        if (!enterPressed) {
          enterPressed = true;
          sendMessage();
        }
      }
    });

    input.addEventListener("keyup", function (e) {
      if (e.key === "Enter") enterPressed = false;
    });

    document.getElementById("sendBtn").addEventListener("click", sendMessage);

    connect();
    loadPreviousMessages();
  });
</script>

</body>
</html>
