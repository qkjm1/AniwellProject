
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

