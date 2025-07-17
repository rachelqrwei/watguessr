<template>
  <section class="testimonials-section" :class="{ loaded: isLoaded }">
    <div class="testimonials-content">
      <h2>WHAT STUDENTS ARE SAYING</h2>
      <div class="testimonials-grid">
        <div class="testimonial-card">
          <div class="testimonial-text">
            "This game made me realize I know way less about campus than I thought! So addictive and fun."
          </div>
          <div class="testimonial-author">
            <div class="author-name">Sarah M.</div>
            <div class="author-program">Computer Science '25</div>
          </div>
        </div>
        <div class="testimonial-card">
          <div class="testimonial-text">
            "Perfect for procrastinating between classes. I've discovered so many cool spots on campus!"
          </div>
          <div class="testimonial-author">
            <div class="author-name">Alex T.</div>
            <div class="author-program">Mathematics '24</div>
          </div>
        </div>
        <div class="testimonial-card">
          <div class="testimonial-text">
            "The daily challenges keep me coming back. Great way to take a study break!"
          </div>
          <div class="testimonial-author">
            <div class="author-name">Jordan K.</div>
            <div class="author-program">Engineering '26</div>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup>
defineProps({
  isLoaded: {
    type: Boolean,
    default: false
  }
})
</script>

<style scoped>
.testimonials-section {
  padding: 80px 40px;
  opacity: 0;
  transform: translateY(30px);
  transition: all 0.8s cubic-bezier(0.25, 0.8, 0.25, 1);
  transition-delay: 1.2s;
  background: var(--dark-grey);
}

.testimonials-section.loaded {
  opacity: 1;
  transform: translateY(0);
}

.testimonials-content {
  max-width: 1200px;
  margin: 0 auto;
  text-align: center;
}

.testimonials-content h2 {
  font-size: 1.8rem;
  font-weight: 800;
  color: var(--white);
  margin-bottom: 30px;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
  letter-spacing: 0.5px;
  text-transform: uppercase;
}

.testimonials-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 30px;
  justify-items: center;
}

.testimonial-card {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 16px;
  padding: 25px 20px;
  text-align: center;
  transition: all 0.3s ease;
}

.testimonial-card:hover {
  transform: translateY(-5px);
  background: rgba(255, 255, 255, 0.08);
}

.testimonial-text {
  font-size: 1rem;
  color: var(--light-grey);
  line-height: 1.6;
  font-weight: 500;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
  margin-bottom: 15px;
}

.testimonial-author {
  text-align: left;
  margin-top: 15px;
}

.author-name {
  font-size: 0.9rem;
  font-weight: 700;
  color: var(--white);
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
  letter-spacing: 0.3px;
}

.author-program {
  font-size: 0.8rem;
  color: var(--light-grey);
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
  margin-top: 5px;
}

@media (max-width: 768px) {
  .testimonials-grid {
    grid-template-columns: 1fr;
  }
}
</style> 