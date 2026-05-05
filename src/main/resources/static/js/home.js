// Sticky Header & Scroll Effect Logic
document.addEventListener('DOMContentLoaded', function () {
    const header = document.getElementById('mainHeader');
    if (!header) return;
    const isTransparentInitial = header.classList.contains('header-transparent');

    function handleScroll() {
        if (window.scrollY > 50) {
            header.classList.add('header-sticky');
            if (isTransparentInitial) {
                header.classList.remove('header-transparent');
                header.classList.add('header-solid');
            }
        } else {
            header.classList.remove('header-sticky');
            if (isTransparentInitial) {
                header.classList.add('header-transparent');
                header.classList.remove('header-solid');
            }
        }
    }

    window.addEventListener('scroll', handleScroll);
    handleScroll();
});

// Snowflake Effect
function createSnowflakes() {
    const container = document.getElementById('snowflake-container');
    const snowflakeCount = 50;
    const snowflakeSymbols = ['❅', '❆', '❇', '✿', '✻'];

    function createSnowflake() {
        const snowflake = document.createElement('div');
        snowflake.classList.add('snowflake');
        snowflake.textContent = snowflakeSymbols[Math.floor(Math.random() * snowflakeSymbols.length)];
        snowflake.style.left = Math.random() * window.innerWidth + 'px';
        snowflake.style.fontSize = (Math.random() * 1 + 0.5) + 'em';
        snowflake.style.opacity = Math.random() * 0.5 + 0.5;

        const duration = Math.random() * 10 + 10;
        const swayChance = Math.random();

        if (swayChance > 0.5) {
            snowflake.classList.add('sway');
            snowflake.style.setProperty('--sway-distance', (Math.random() * 100 - 50) + 'px');
        }

        snowflake.style.animation = `fall ${duration}s linear forwards`;

        container.appendChild(snowflake);

        setTimeout(() => snowflake.remove(), duration * 1000);
    }

    // Create initial snowflakes
    for (let i = 0; i < snowflakeCount; i++) {
        setTimeout(() => {
            createSnowflake();
        }, i * 100);
    }

    // Keep creating snowflakes continuously
    setInterval(() => {
        createSnowflake();
    }, 200);
}

document.addEventListener('DOMContentLoaded', createSnowflakes);