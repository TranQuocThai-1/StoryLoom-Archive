async function fetchAndDisplayRegions() {
    try {
        const response = await fetch('/api/regions');
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

// ==========================================
// --- AI GLOBAL WIDGET LOGIC ---
// ==========================================

function toggleAiChat() {
    const widget = document.getElementById('ai-chat-widget');
    const btn = document.getElementById('ai-toggle-btn');
    widget.classList.toggle('ai-widget-collapsed');
    btn.innerText = widget.classList.contains('ai-widget-collapsed') ? '▲' : '▼';
}

function handleAiKeyPress(e) {
    if (e.key === 'Enter') sendAiMessage();
}

async function sendAiMessage() {
    const input = document.getElementById('ai-user-input');
    const history = document.getElementById('ai-chat-history');
    const text = input.value.trim();
    
    if (!text) return;

    history.innerHTML += `<div class="ai-message ai-user">${text}</div>`;
    input.value = '';
    
    const responseId = 'ai-resp-' + Date.now();
    history.innerHTML += `<div id="${responseId}" class="ai-message ai-system">...</div>`;
    history.scrollTop = history.scrollHeight;

    try {
        const response = await fetch('/api/ai/chat', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ prompt: text })
        });
        
        const reader = response.body.getReader();
        const decoder = new TextDecoder("utf-8");
        let fullResponse = "";

        while (true) {
            const { value, done } = await reader.read();
            if (done) break;
            
            const chunk = decoder.decode(value, { stream: true });
            const lines = chunk.split("\n");
            
            for (let line of lines) {
                if (line.startsWith("data:")) {
                    fullResponse += line.replace("data:", "");
                    document.getElementById(responseId).innerHTML = fullResponse.replace(/\n/g, '<br>');
                    history.scrollTop = history.scrollHeight;
                }
            }
        }
    } catch (error) {
        document.getElementById(responseId).innerText = "Error connecting to AI.";
    }
}