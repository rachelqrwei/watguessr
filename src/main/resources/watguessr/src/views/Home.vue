<script setup>
import Header from '../components/Header.vue';

</script>
<template>
    <Header />
    <div class="flex-container home-description-container">
        <div class="placeholder-square"></div>

        <div :style="{width: '50%'}">
            <div class="title">
                <span class="highlight">WATGUESSR</span><span>.IO</span>
            </div>
            <p class="subtitle">
                The hit game Geoguessr, built specifically for the University of Waterloo.
            </p>
            <hr />
        </div>
    </div>

    <div :style="{marginTop: '50px'}">
        <h4>PLAY WATGUESSR</h4>
        <div class="play-options-container flex-container">
            <div class="player-1-trapezoid">
                <div class="play-option-container">
                    <h3>SOLO</h3>
                    <p>PLAY BY YOURSELF UNTIL YOUR POINTS RUN OUT!</p>
                </div>
            </div>

            <div class="player-2-trapezoid">
                <div class="play-option-container">
                    <h3>DUELS</h3>
                    <p>PLAY AGAINST A FRIEND AND COMPETE FOR POINTS!</p>
                </div>
            </div>
        </div>
    </div>
</template>
<script>

</script>
<style scoped>
.home-description-container {
    margin-top: 100px;
    align-items: center;
}

span {
    font-size: 50px;
    font-weight: bold;
    color: var(--light-grey)
}

.highlight {
    color: var(--yellow);
}

.subtitle {
    font-size: 20px;
    color: var(--light-grey);
    line-height: 30px;
    font-weight: bold;
}

hr {
    margin-top: 20px;
    color: var(--light-grey);
    border-width: 2px;
    width: 25%;
}

.placeholder-square {
    width: 235px;
    height: 235px;
    background-color: var(--light-grey); /* Or any color */
    border: 2px dashed #ccc; /* Optional: dashed border */
    display: flex;
    align-items: center;
    justify-content: center;
    color: #666;
    margin: 30px;
}

.player-1-trapezoid {
    position: relative;
    width: 350px;
    height: 150px;
    clip-path: polygon(0 0, 100% 0, 80% 100%, 0% 100%);
    border-radius: 10px 5px 20px 10px;
    padding: 20px;
    overflow: hidden; /* important to clip the ::before content */
    z-index: 0;
}

.player-1-trapezoid::before {
    content: "";
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background-image: var(--player-1-gradient);
    opacity: 0.8;
    z-index: -1;
}

.player-2-trapezoid {
    position: relative;
    width: 350px;
    height: 150px;
    left: -50px;
    clip-path: polygon(20% 0%, 100% 0%, 100% 100%, 0% 100%);
    border-radius: 10px 5px 20px 10px;
    padding: 20px;
    padding-left: 80px;
    overflow: hidden; /* important to clip the ::before content */
    z-index: 0;
}

.player-2-trapezoid::before {
    content: "";
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background-image: var(--player-2-gradient);
    opacity: 0.8;
    z-index: -1;
}

.play-option-container {
    width: 80%;
    font-size: 14px;
}

.play-option-container h3{
    font-weight: bold;
    font-size: 35px;
}

.play-option-container p {
    font-size: 14px;
    font-weight: bold;
}

h4 {
    font-weight: bold;
    font-size: 16px;
    margin-bottom: 10px;
}

</style>