// Select interactive elements
const glassCard = document.querySelector('.glass-card');
const orb = document.querySelector('.glow-orb');

// Dynamic 3D effect on the glass card
document.addEventListener('mousemove', (e) => {
  if (!glassCard) return;
  const xAxis = (window.innerWidth / 2 - e.pageX) / 25;
  const yAxis = (window.innerHeight / 2 - e.pageY) / 25;
  
  glassCard.style.transform = `rotateY(${xAxis}deg) rotateX(${yAxis}deg) scale(1.05)`;
});

// Reset transform on mouse leave
document.addEventListener('mouseleave', () => {
  if (!glassCard) return;
  glassCard.style.transform = `rotateY(0deg) rotateX(0deg) scale(1)`;
});

// Follow cursor effect for the glow orb
document.addEventListener('mousemove', (e) => {
  if (!orb) return;
  // Make the orb slowly follow the mouse within the visual container bounds
  const visualBounds = document.querySelector('.hero-visual').getBoundingClientRect();
  
  if (e.clientX >= visualBounds.left && e.clientX <= visualBounds.right &&
      e.clientY >= visualBounds.top && e.clientY <= visualBounds.bottom) {
    const x = e.clientX - visualBounds.left - 100;
    const y = e.clientY - visualBounds.top - 100;
    
    orb.style.transform = `translate(${x * 0.1}px, ${y * 0.1}px)`;
  }
});
