(function () {
    const form = document.getElementById('chat-form');
    const questionInput = document.getElementById('question');
    const submitBtn = document.getElementById('submit-btn');
    const answerEl = document.getElementById('answer');
    const sourcesEl = document.getElementById('sources');

    const SOURCE_PATH = {pokemon: '/pokemon/', town: '/town/', gym: '/gym/'};

    form.addEventListener('submit', function (event) {
        event.preventDefault();
        const question = questionInput.value.trim();
        if (!question) {
            return;
        }

        submitBtn.disabled = true;
        submitBtn.textContent = '답변 생성 중...';
        answerEl.classList.remove('error');
        answerEl.textContent = '';
        sourcesEl.textContent = '';

        fetch('/api/chat', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({question})
        })
            .then(function (res) {
                return res.json().then(function (body) {
                    return {ok: res.ok, body};
                });
            })
            .then(function (result) {
                if (result.ok) {
                    renderAnswer(result.body);
                } else {
                    renderError(result.body.message);
                }
            })
            .catch(function () {
                renderError('요청을 처리하지 못했습니다. 잠시 후 다시 시도해주세요.');
            })
            .finally(function () {
                submitBtn.disabled = false;
                submitBtn.textContent = '질문하기';
            });
    });

    function renderAnswer(response) {
        answerEl.textContent = response.answer;
        (response.sources || []).forEach(function (source) {
            const link = document.createElement('a');
            link.className = 'source-chip';
            link.href = (SOURCE_PATH[source.type] || '#') + source.id;
            link.textContent = source.title;
            sourcesEl.appendChild(link);
        });
    }

    function renderError(message) {
        answerEl.classList.add('error');
        answerEl.textContent = message || '오류가 발생했습니다.';
    }
})();
