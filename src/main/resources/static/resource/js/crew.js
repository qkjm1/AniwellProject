
   const script = document.createElement("script");
   script.src = `https://dapi.kakao.com/v2/maps/sdk.js?appkey=${kakaoJsKey}&autoload=false&libraries=services`;
   const kakaoJsKey = [[${kakaoJsKey}]];
   script.onload = () => {
     kakao.maps.load(initLocation);
   };
   document.head.appendChild(script);

   function initLocation() {
     if (navigator.geolocation) {
       navigator.geolocation.getCurrentPosition(function (position) {
         const lat = position.coords.latitude;
         const lng = position.coords.longitude;

         const geocoder = new kakao.maps.services.Geocoder();
         geocoder.coord2RegionCode(lng, lat, function (result, status) {
           if (status === kakao.maps.services.Status.OK) {
             for (let i = 0; i < result.length; i++) {
               if (result[i].region_type === "B") {
                 const fullAddr = result[i].address_name;
                 document.getElementById("currentLocation").innerText = fullAddr;

                 const parts = fullAddr.split(" ");
                 if (parts.length >= 3) {
                   const city = parts[0];
                   const district = parts[1];
                   loadDongList(city, district);
                 }
                 break;
               }
             }
           } else {
             document.getElementById("currentLocation").innerText = "위치 정보 불러오기 실패";
           }
         });
       }, function () {
         document.getElementById("currentLocation").innerText = "위치 접근 거부됨";
       });
     } else {
       document.getElementById("currentLocation").innerText = "GPS를 지원하지 않는 브라우저입니다.";
     }
   }

   function loadDongList(city, district) {
     const url = "/usr/walkCrew/getDongs?city=" + encodeURIComponent(city) + "&district=" + encodeURIComponent(district);

     fetch(url)
       .then(response => response.json())
       .then(data => {
         const container = document.getElementById("dongListContainer");
         container.innerHTML = "";

         if (data.length === 0) {
           container.innerText = "해당 지역의 동 정보가 없습니다.";
           return;
         }

         data.forEach(dong => {
           const btn = document.createElement("button");
           btn.type = "button";
           btn.innerText = dong;
           btn.onclick = () => {
             document.getElementById("selectedDong").value = dong;

             document.querySelectorAll(".dong-list button").forEach(b => {
               b.style.backgroundColor = "";
             });
             btn.style.backgroundColor = "#ddd";

             // ✅ 선택한 동으로부터 districtId 조회
             fetch("/usr/walkCrew/getDistrictId?dong=" + encodeURIComponent(dong))
               .then(response => response.text())
               .then(districtId => {
                 document.getElementById("districtIdInput").value = districtId;
               });
           };
           container.appendChild(btn);
         });
       })
       .catch(err => {
         document.getElementById("dongListContainer").innerText = "동 정보 로딩 실패";
         console.error("Error loading dongs:", err);
       });
   }
