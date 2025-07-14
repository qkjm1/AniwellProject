
function renderMiniCalendar(targetId, date = new Date()) {
	const container = document.getElementById(targetId);
	container.innerHTML = ''; // 기존 초기화

	const year = date.getFullYear();
	const month = date.getMonth();

	const firstDay = new Date(year, month, 1).getDay();
	const lastDate = new Date(year, month + 1, 0).getDate();
	const prevLastDate = new Date(year, month, 0).getDate();

	const today = new Date();

	// 상단 컨트롤
	const header = document.createElement('div');
	header.className = 'flex justify-between items-center mb-4 text-yellow-200';

	const prevBtn = document.createElement('button');
	prevBtn.innerHTML = '&lt;';
	prevBtn.className = 'text-black hover:text-yellow-800';
	const nextBtn = document.createElement('button');
	nextBtn.innerHTML = '&gt;';
	nextBtn.className = 'text-black hover:text-yellow-800';
	const title = document.createElement('h2');
	title.className = 'text-lg font-bold text-center text-black flex-1';
	title.textContent = `${year}년 ${month + 1}월`;

	prevBtn.onclick = () => {
		const newDate = new Date(year, month - 1, 1);
		renderMiniCalendar(targetId, newDate);
	};
	nextBtn.onclick = () => {
		const newDate = new Date(year, month + 1, 1);
		renderMiniCalendar(targetId, newDate);
	};

	header.appendChild(prevBtn);
	header.appendChild(title);
	header.appendChild(nextBtn);
	container.appendChild(header);

	// 요일 헤더
	const weekRow = document.createElement('div');
	weekRow.className = 'grid grid-cols-7 text-center text-sm font-semibold text-gray-700 mb-2';
	['일', '월', '화', '수', '목', '금', '토'].forEach(day => {
		const div = document.createElement('div');
		div.textContent = day;
		weekRow.appendChild(div);
	});
	container.appendChild(weekRow);

	// 날짜
	const grid = document.createElement('div');
	grid.className = 'grid grid-cols-7 text-center text-gray-900 gap-y-2';

	// 지난달 날짜
	for (let i = firstDay - 1; i >= 0; i--) {
		const d = prevLastDate - i;
		const el = document.createElement('div');
		el.textContent = d;
		el.className = 'text-gray-400';
		grid.appendChild(el);
	}

	// 이번 달 날짜
	for (let i = 1; i <= lastDate; i++) {
		const el = document.createElement('div');
		el.textContent = i;
		if (
			i === today.getDate() &&
			month === today.getMonth() &&
			year === today.getFullYear()
		) {
			el.className = 'bg-yellow-400 rounded-full font-bold';
		}
		grid.appendChild(el);
	}

	// 다음달 빈칸
	const total = firstDay + lastDate;
	const nextCells = 7 - (total % 7);
	if (nextCells < 7) {
		for (let i = 1; i <= nextCells; i++) {
			const el = document.createElement('div');
			el.textContent = i;
			el.className = 'text-gray-400';
			grid.appendChild(el);
		}
	}


	grid.className = "grid grid-cols-7 grid-rows-6 gap-y-1 text-center";


	container.appendChild(grid);
}


