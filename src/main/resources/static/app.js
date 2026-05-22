// --- Waiter Logic ---
async function fetchAndDisplayRegions() {
    try {
        const response = await fetch('http://localhost:8082/api/regions');
    } catch (error) {
        console.error("The Waiter tripped!", error);
    }
}
fetchAndDisplayRegions();

document.addEventListener('DOMContentLoaded', () => {
    const sliderWrappers = document.querySelectorAll('.slider-wrapper');
    sliderWrappers.forEach(wrapper => {
        const slider = wrapper.querySelector('.book-slider');
        const leftBtn = wrapper.querySelector('.left-btn');
        const rightBtn = wrapper.querySelector('.right-btn');
        const scrollAmount = 340; 

        if(leftBtn && rightBtn && slider) {
            leftBtn.addEventListener('click', () => {
                slider.scrollBy({ left: -scrollAmount, behavior: 'smooth' });
            });

            rightBtn.addEventListener('click', () => {
                slider.scrollBy({ left: scrollAmount, behavior: 'smooth' });
            });
        }
    });

    const allLinks = document.querySelectorAll('a');
    allLinks.forEach(link => {
        link.addEventListener('click', function() {
            this.classList.add('visited-temp');
        });
    });

    const helpBtn = document.getElementById('open-help-btn');
    const closeBtn = document.getElementById('close-help-btn');
    const modal = document.getElementById('help-modal');

    if (helpBtn && closeBtn && modal) {
        helpBtn.addEventListener('click', (e) => {
            e.preventDefault(); 
            modal.style.display = 'flex';
        });

        closeBtn.addEventListener('click', () => {
            modal.style.display = 'none';
        });

        window.addEventListener('click', (event) => {
            if (event.target === modal) {
                modal.style.display = 'none';
            }
        });
    }

    const tabs = document.querySelectorAll('.search-tab[data-tab]');
    const contents = document.querySelectorAll('.tab-content');

    if(tabs.length > 0) {
        tabs.forEach(tab => {
            tab.addEventListener('click', () => {
                
                tabs.forEach(t => t.classList.remove('active'));
                contents.forEach(c => {
                    c.classList.remove('active');
                });

                tab.classList.add('active');

                const targetId = tab.getAttribute('data-tab');
                const targetContent = document.getElementById(targetId);
                
                if (targetContent) {
                    targetContent.classList.add('active');
                }
            });
        });
    }
});