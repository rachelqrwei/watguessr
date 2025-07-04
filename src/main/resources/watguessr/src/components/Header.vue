<template>
    <div class="header-container">
        <div class="streak-container flex-container">
            <img src="../assets/images/Header/streak-icon.png" alt="Streak" />
            <p>1</p>
        </div>

        <div class="profile-container flex-container">
            <img src="../assets/images/Header/generic-avatar.png" alt="Profile Icon" />
            <p>RACHEL W</p>
            <button>
                <img src="../assets/images/Header/drop-down.png" alt="Drop down" />
            </button>
        </div>
    </div>

</template>
<script>
</script>
<style scoped>
.header-container {
    display: flex;
    justify-content: flex-end;
    gap: 50px;
    position: absolute;
    top: 0;
    right: 0;
    padding: 30px;
}

.streak-container {
    align-items: center;
    gap: 10px;
}

.streak-container p {
    font-weight: bold;
    font-size: 16px;
    background: var(--player-1-gradient);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
}

.profile-container {
    align-items: center;
    gap: 10px;
}

.profile-container p {
    font-weight: bold;
    font-size: 16px;
}

.profile-container img {
    height: 36px;
}
</style>
