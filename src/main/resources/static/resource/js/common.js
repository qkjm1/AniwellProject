
const paw = document.getElementById('cat-paw');
const btn = document.getElementById('hamburger-btn');

let isVisible = false;

btn.addEventListener('click', () => {
	isVisible = !isVisible;
	if (isVisible) {
	      paw.classList.remove('left-[-100%]');
	      paw.classList.add('left-0');
	    } else {
	      paw.classList.remove('left-0');
	      paw.classList.add('left-[-100%]');
	    }
});


// sidebar.js
document.querySelectorAll('.menu-item').forEach((item) => {
  item.addEventListener('click', () => {
    const page = item.dataset.page
    let url = ''

    switch (page) {
      case 'pet':
        url = `/usr/pet/petPage?petId=1` // 로그인 ID로 교체 가능
        break
      case 'my':
        url = `/usr/pet/list?memberId=1`
        break
      case 'crew':
        url = `/crew`
        break
    }

    window.parent.location.href = url
  })
})

