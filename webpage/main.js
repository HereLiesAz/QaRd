// Select interactive elements
const phoneMockup = document.querySelector('.phone-mockup');
const orb = document.querySelector('.glow-orb');
const heroContent = document.querySelector('.hero-content');
const featureCards = document.querySelectorAll('.feature-card');

let mouseX = 0;
let mouseY = 0;

// Dynamic 3D effect on the phone mockup (mouse movement)
document.addEventListener('mousemove', (e) => {
  mouseX = e.clientX;
  mouseY = e.clientY;
  
  if (!phoneMockup) return;
  const xAxis = (window.innerWidth / 2 - e.pageX) / 25;
  const yAxis = (window.innerHeight / 2 - e.pageY) / 25;
  
  const parallaxY = window.scrollY * 0.2;
  phoneMockup.style.transform = `translateY(${parallaxY}px) rotateY(${xAxis}deg) rotateX(${yAxis}deg) scale(1.02)`;
});

// Reset transform on mouse leave
document.addEventListener('mouseleave', () => {
  if (!phoneMockup) return;
  const parallaxY = window.scrollY * 0.2;
  phoneMockup.style.transform = `translateY(${parallaxY}px) rotateY(0deg) rotateX(0deg) scale(1)`;
});

// Scroll Parallax animations
window.addEventListener('scroll', () => {
  const scrollY = window.scrollY;
  
  // Parallax for hero text
  if (heroContent) {
    heroContent.style.transform = `translateY(${scrollY * 0.3}px)`;
    heroContent.style.opacity = 1 - (scrollY / 500);
  }
  
  // Parallax for the phone mockup
  if (phoneMockup) {
    phoneMockup.style.transform = `translateY(${scrollY * 0.2}px)`;
  }

  // Parallax for the glow orb
  if (orb) {
    orb.style.transform = `translateY(${scrollY * 0.5}px)`;
  }
  
  // Fade and slide up for feature cards based on scroll position
  featureCards.forEach((card, index) => {
    const cardTop = card.getBoundingClientRect().top;
    const windowHeight = window.innerHeight;
    
    if (cardTop < windowHeight * 0.85) {
      card.style.opacity = '1';
      card.style.transform = `translateY(0)`;
    }
  });
});

// Initial state for feature cards
featureCards.forEach((card, index) => {
  card.style.opacity = '0';
  card.style.transform = `translateY(${50 + (index * 20)}px)`;
  card.style.transition = 'all 0.8s cubic-bezier(0.165, 0.84, 0.44, 1)';
});
