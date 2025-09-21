// Main JavaScript file for EduKids

document.addEventListener('DOMContentLoaded', function() {

    // --- Vocabulary Card Audio Simulation ---
    const vocabCards = document.querySelectorAll('.vocab-card');

    vocabCards.forEach(card => {
        card.addEventListener('click', function() {
            const audioSrc = this.dataset.audioSrc;
            if (audioSrc) {
                // In a real application, you would use the Web Audio API.
                // Since audio files are not available, we'll show an alert.
                alert(`Playing audio for: ${this.querySelector('.vocab-en').textContent}\n(Audio file not found: ${audioSrc})`);

                // Add a visual feedback class
                this.classList.add('playing');
                setTimeout(() => {
                    this.classList.remove('playing');
                }, 300); // Remove class after animation
            }
        });
    });

    // --- Spelling Exercise ---
    const checkSpellBtn = document.getElementById('check-spell-btn');
    if (checkSpellBtn) {
        checkSpellBtn.addEventListener('click', function() {
            const inputs = document.querySelectorAll('#spell-exercise .spell-input');
            inputs.forEach(input => {
                const userAnswer = input.value.toLowerCase().trim();
                const correctAnswer = input.dataset.answer;

                if (userAnswer === correctAnswer) {
                    input.classList.remove('incorrect');
                    input.classList.add('correct');
                } else {
                    input.classList.remove('correct');
                    input.classList.add('incorrect');
                }
            });
        });
    }

    // --- Reusable Quiz Logic ---
    function initializeQuiz(containerSelector) {
        const quizContainer = document.querySelector(containerSelector);
        if (!quizContainer) return;

        const quizItems = quizContainer.querySelectorAll('.quiz-item');
        quizItems.forEach(item => {
            const choices = item.querySelectorAll('.choice-btn');
            choices.forEach(choice => {
                choice.addEventListener('click', function() {
                    if (item.classList.contains('answered')) return;

                    item.classList.add('answered');
                    this.classList.add('selected');

                    let userAnswer = '';
                    if (this.classList.contains('tick')) userAnswer = 'tick';
                    else if (this.classList.contains('cross')) userAnswer = 'cross';
                    else if (this.classList.contains('yes')) userAnswer = 'yes';
                    else if (this.classList.contains('no')) userAnswer = 'no';

                    const correctAnswer = item.dataset.correct;

                    if (userAnswer === correctAnswer) {
                        this.classList.add('correct');
                    } else {
                        this.classList.add('incorrect');
                    }
                });
            });
        });
    }

    initializeQuiz('#tick-cross-quiz');
    initializeQuiz('#yes-no-quiz');
    initializeQuiz('#robin-quiz');
    initializeQuiz('#y-sound-quiz');

    // --- Drag and Drop Game ---
    const dndGame = document.getElementById('dnd-game');
    if (dndGame) {
        const startersContainer = document.getElementById('dnd-starters');
        const endersContainer = document.getElementById('dnd-enders');
        const feedbackEl = document.getElementById('dnd-feedback');

        const words = [
            { start: 'Satur', end: 'day' },
            { start: 'bed', end: 'room' },
            { start: 'Wednes', end: 'day' },
            { start: 'Thurs', end: 'day' },
            { start: 'bath', end: 'room' },
            { start: 'Sun', end: 'day' },
            { start: 'class', end: 'room' },
            { start: 'Tues', end: 'day' }
        ];

        let correctMatches = 0;

        function shuffle(array) {
            for (let i = array.length - 1; i > 0; i--) {
                const j = Math.floor(Math.random() * (i + 1));
                [array[i], array[j]] = [array[j], array[i]];
            }
            return array;
        }

        function setupGame() {
            startersContainer.innerHTML = '';
            endersContainer.innerHTML = '';
            const shuffledEnders = shuffle([...words]);

            words.forEach((word) => {
                const starterEl = document.createElement('div');
                starterEl.classList.add('dnd-starter');
                starterEl.dataset.match = word.end;
                starterEl.dataset.start = word.start;
                starterEl.innerHTML = `<span>${word.start}-</span><span class="drop-part">____</span>`;
                startersContainer.appendChild(starterEl);
            });

            shuffledEnders.forEach((word, index) => {
                const enderEl = document.createElement('div');
                enderEl.classList.add('dnd-draggable');
                enderEl.draggable = true;
                enderEl.dataset.value = word.end;
                enderEl.id = `drag-${index}`;
                enderEl.textContent = `-${word.end}`;
                endersContainer.appendChild(enderEl);
            });

            addDragListeners();
            addDropListeners();
        }

        function addDragListeners() {
            const draggables = document.querySelectorAll('.dnd-draggable');
            draggables.forEach(draggable => {
                draggable.addEventListener('dragstart', (e) => {
                    e.dataTransfer.setData('text/plain', e.target.id);
                    setTimeout(() => draggable.classList.add('dragging'), 0);
                });
                draggable.addEventListener('dragend', () => {
                    draggable.classList.remove('dragging');
                });
            });
        }

        function addDropListeners() {
            const starters = document.querySelectorAll('.dnd-starter');
            starters.forEach(starter => {
                starter.addEventListener('dragover', (e) => {
                    if (!starter.classList.contains('correct')) {
                        e.preventDefault();
                        starter.classList.add('drag-over');
                    }
                });
                starter.addEventListener('dragleave', () => {
                    starter.classList.remove('drag-over');
                });
                starter.addEventListener('drop', (e) => {
                    e.preventDefault();
                    starter.classList.remove('drag-over');
                    if (starter.classList.contains('correct')) return;

                    const id = e.dataTransfer.getData('text/plain');
                    const draggable = document.getElementById(id);

                    if (starter.dataset.match === draggable.dataset.value) {
                        starter.classList.add('correct');
                        starter.innerHTML = `${starter.dataset.start}${draggable.textContent}`;
                        draggable.draggable = false;
                        draggable.style.display = 'none';
                        correctMatches++;
                        feedbackEl.textContent = '正确！';
                        if (correctMatches === words.length) {
                            feedbackEl.textContent = '太棒了，你完成了所有匹配！';
                        }
                    } else {
                        feedbackEl.textContent = '不对哦，再试一次！';
                    }
                     setTimeout(() => { if (correctMatches !== words.length) feedbackEl.textContent = ''; }, 1500);
                });
            });
        }

        setupGame();
    }

    // --- Sticky Nav and Active Section Highlighting ---
    const lessonNav = document.getElementById('lesson-nav-links');
    const sections = document.querySelectorAll('.learning-section');

    if (lessonNav && sections.length > 0) {
        // 1. Generate Nav Links
        sections.forEach(section => {
            const sectionTitle = section.querySelector('h2').textContent;
            const sectionId = section.id;
            if (sectionId) {
                const li = document.createElement('li');
                const a = document.createElement('a');
                a.textContent = sectionTitle;
                a.href = `#${sectionId}`;
                li.appendChild(a);
                lessonNav.appendChild(li);
            }
        });

        // 2. Intersection Observer for active state
        const navLinks = lessonNav.querySelectorAll('a');
        const observer = new IntersectionObserver(entries => {
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    const id = entry.target.id;
                    navLinks.forEach(link => {
                        link.classList.toggle('active', link.getAttribute('href') === `#${id}`);
                    });
                }
            });
        }, { rootMargin: '-40% 0px -60% 0px' });

        sections.forEach(section => {
            if (section.id) {
                observer.observe(section);
            }
        });
    }

});
