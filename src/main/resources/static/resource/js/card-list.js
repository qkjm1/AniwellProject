
// ✅ PetCard 컴포넌트 정의
function PetCard({ pet }) {
	return (
		<div className="ani-card bg-white aspect-[1/1.6] w-[420px] rounded-xl p-6 shadow">
			<h2 className="text-2xl font-bold flex items-center gap-1 mb-4 justify-center">
				<span className="text-3xl">🐾</span> 반려동물등록증
			</h2>
			<div className="grid grid-cols-3">
				<div>
					<div className="space-y-1 text-sm">
						<div><span className="font-semibold mr-2">이름:</span> {pet.name}</div>
						<div><span className="font-semibold mr-2">번호:</span> {pet.id}</div>
						<div><span className="font-semibold mr-2">품종:</span> {pet.breed}</div>
						<div><span className="font-semibold mr-2">성별:</span> {pet.gender}</div>
					</div>
				</div>
				<div className="space-y-1 text-sm">
					<div><span className="font-semibold mr-2">생일:</span> {pet.birthDate}</div>
					<div><span className="font-semibold mr-2">중성화:</span> 완료</div>
				</div>
				<div className="flex justify-center items-start">
					<img src={pet.photo} alt="cat" className="w-[100px] h-[100px] object-cover rounded-xl border" />
				</div>
			</div>
			<div className="text-sm ">
				<span className="font-semibold mr-2">특징:</span> {pet.species}
			</div>
			<div className="createdAt text-right text-xs text-gray-500 mt-2">{pet.createdAt}</div>
		</div>
	);
}

function App() {
	const [pets, setPets] = React.useState([]);
	const [loginedMember, setLoginedMember] = React.useState(null);

	React.useEffect(() => {
		fetch('/usr/member/myPage')
			.then(res => res.json())
			.then((data) => {
				console.log(data);
				setLoginedMember(data);
				
				window.localStorage.setItem('loginedMember', data.id);
			});
	}, []);

	React.useEffect(() => {
		if (loginedMember && loginedMember.id) {
			fetch(`/api/pets?memberId=${loginedMember.id}`)
				.then(res => res.json())
				.then((data) => {
					console.log(data);
					console.log("petlist: ", data.data2);
					setPets(data.data2 || []);
				});
		}
	}, [loginedMember]);

	const TOTAL_CARD_COUNT = 3;
	const petList = [...pets, ...Array(TOTAL_CARD_COUNT - pets.length).fill(null)];

	React.useEffect(() => {
		const swiper = new Swiper('.mySwiper', {
			slidesPerView: 'auto',
			centeredSlides: true,
			spaceBetween: -160,
			loop: false,
			grabCursor: true,
			on: {
				slideChangeTransitionEnd() {
					const allSlides = document.querySelectorAll('.swiper-slide');
					const browserCenterX = window.innerWidth / 2;

					let closestSlide = null;
					let closestIndex = 0;
					let closestDistance = Infinity;

					allSlides.forEach((slide, idx) => {
						const rect = slide.getBoundingClientRect();
						const slideCenter = rect.left + rect.width / 2;
						const distance = Math.abs(slideCenter - browserCenterX);

						if (distance < closestDistance) {
							closestDistance = distance;
							closestSlide = slide;
							closestIndex = idx;
						}
					});

					// 👉 중앙 슬라이드의 petId를 찾기 위해 data-속성 활용 추천
					const petId = closestSlide.getAttribute("data-pet-id");
					const pet = pets.find(p => p.id.toString() === petId);

					if (pet) {
						console.log("✅ 현재 중앙에 있는 펫:", pet.id, pet.name);
						window.localStorage.setItem('selectedPetId', pet.id);
					} else {
						console.log("❗ 중앙 카드가 비어있습니다.");
						localStorage.removeItem('selectedPetId')
					}

					const distantIndices = [];
					for (let i = 0; i < allSlides.length; i++) {
						if (Math.abs(i - closestIndex) >= 2) {
							distantIndices.push(i);
						}
					}

					allSlides.forEach((slide, idx) => {
						if (!distantIndices.includes(idx)) {
							if (slide.classList.contains('is-distant')) {
								slide.classList.remove('is-distant');
								slide.classList.add('is-distant-leaving');
								setTimeout(() => {
									slide.classList.remove('is-distant-leaving');
								}, 500);
							}
						}
					});

					requestAnimationFrame(() => {
						distantIndices.forEach(index => {
							const slide = swiper.slides[index];
							if (slide) {
								slide.classList.remove('is-distant-leaving');
								slide.classList.add('is-distant');
							}
						});
					});
				},
			}
		});

		setTimeout(() => {
			swiper.emit('slideChangeTransitionEnd');
			console.log('초기 흐림효과 적용');
		}, 10);
	}, [pets]);

	return (
		<div className="w-[1000px] mx-auto flex justify-center overflow-visible">
			<div className="swiper mySwiper max-w-[1000px] overflow-visible">
				<div className="swiper-wrapper">
					{petList.map((pet, idx) =>
						pet ? (
							<div className="swiper-slide w-[30vw] max-w-[1000px]"
								key={pet.id}
								data-pet-id={pet.id}>
								<PetCard pet={pet} />
							</div>
						) : (
							<div className="swiper-slide w-[30vw] max-w-[1000px] opacity-30 flex justify-center" key={`empty-${idx}`}>
								<div className="ani-card bg-gray-100 aspect-[1/1.6] w-[400px] rounded-xl p-6 shadow flex items-center justify-center text-gray-500">
									등록된 펫이 없습니다
								</div>
							</div>
						)
					)}
				</div>
			</div>
		</div>
	);
}

const root = ReactDOM.createRoot(document.getElementById("root"));
root.render(<App />);
