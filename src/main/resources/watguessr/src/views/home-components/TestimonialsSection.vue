<template>
  <section class="testimonials-section" :class="{ loaded: isLoaded }">
    <div class="testimonials-content">
      <h2>WHAT STUDENTS ARE SAYING</h2>
      <div class="testimonials-grid">
        <div class="testimonial-card">
          <div class="testimonial-text">
            "This game saved my cat from a burning building"
          </div>
          <div class="testimonial-author">
            <div class="author-name">Toph C.</div>
            <div class="author-program">Computer Science </div>
          </div>
        </div>
        <div class="testimonial-card">
          <div class="testimonial-text">
            "Perfect for procrastinating between classes. I've discovered so many cool spots on campus!"
          </div>
          <div class="testimonial-author">
            <div class="author-name">Dylan D.</div>
            <div class="author-program">Computer Science </div>
          </div>
        </div>
        <div class="testimonial-card">
          <div class="testimonial-text">
            "I thought I knew Waterloo's campus… this game humbled me."
          </div>
          <div class="testimonial-author">
            <div class="author-name">Sooyeun K.</div>
            <div class="author-program">Systems Design Engineering</div>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<script>
export default {
  props: {
    isLoaded: {
      type: Boolean,
      default: false
    }
  }
}
</script>

<style scoped>
.testimonials-section {
  padding: 72px 36px;
  opacity: 0;
  transform: translateY(30px);
  transition: all 0.8s cubic-bezier(0.25, 0.8, 0.25, 1);
  transition-delay: 1.2s;
}

.testimonials-section.loaded {
  opacity: 1;
  transform: translateY(0);
}

.testimonials-content {
  max-width: 960px;
  margin: 0 auto;
  text-align: center;
}

.testimonials-content h2 {
  font-weight: 600;
  font-size: 1.6rem;
  color: var(--white);
  text-transform: uppercase;
  margin-bottom: 1.5rem;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
  letter-spacing: 1.5px;
  line-height: 1.5;
}

.testimonials-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 27px;
  justify-items: center;
}

.testimonial-card {
  background: rgba(42, 42, 44, 0.65);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 16px;
  padding: 23px 18px;
  text-align: center;
  backdrop-filter: blur(8px);
  transition: all 0.3s ease;
}

.testimonial-card:hover {
  transform: translateY(-5px);
  background: rgba(42, 42, 44, 0.75);
}

.testimonial-text {
  font-family: "Red Hat Text", sans-serif;
  font-style: normal;
  font-weight: 400;
  font-size: 0.9rem;
  letter-spacing: 1.0px;
  color: var(--light-grey);
  line-height: 1.6;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
  margin-bottom: 14px;
}

.testimonial-author {
  text-align: left;
  margin-top: 18px;
  font-weight: 600;
  color: var(--white);
  text-transform: uppercase;
  margin-bottom: 8px;
  letter-spacing: 1.2px;
}

.author-name {
  font-size: 0.81rem;
  font-weight: 700;
  color: var(--white);
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
  letter-spacing: 1px;
}

.author-program {
  font-size: 0.72rem;
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
