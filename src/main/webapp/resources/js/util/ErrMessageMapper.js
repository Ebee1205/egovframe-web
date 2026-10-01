(function (global) {
    'use strict';

    // data-error-for 값과 form의 필드 이름을 연결한다.
    function create(form, formError) {
        function setFormError(message) {
            if (!formError) { return; }
            formError.textContent = message || '';
            formError.classList.toggle('d-none', !message);
        }

        function clear() {
            form.querySelectorAll('[data-error-for]').forEach(function (output) {
                output.textContent = '';
                var input = form.elements[output.getAttribute('data-error-for')];
                if (input) {
                    input.classList.remove('is-invalid');
                    input.removeAttribute('aria-invalid');
                }
            });
            setFormError('');
        }

        function show(errors, message) {
            errors = errors || {};
            form.querySelectorAll('[data-error-for]').forEach(function (output) {
                var field = output.getAttribute('data-error-for');
                var messages = errors[field] || [];
                if (!Array.isArray(messages)) { messages = [messages]; }
                output.textContent = messages.join('\n');
                var input = form.elements[field];
                if (input) {
                    input.classList.toggle('is-invalid', messages.length > 0);
                    input.setAttribute('aria-invalid', messages.length > 0 ? 'true' : 'false');
                }
            });
            setFormError(message);
        }

        // Commons Validator가 전달한 규칙에서 필드명을 가져온다.
        function showValidationErrors(messages, rules) {
            var errors = Object.create(null);
            Object.keys(rules || {}).forEach(function (key) {
                var rule = rules[key];
                if (messages.indexOf(rule[1]) >= 0) {
                    (errors[rule[0]] || (errors[rule[0]] = [])).push(rule[1]);
                }
            });
            show(errors);
        }

        return {
            clear: clear,
            show: show,
            showValidationErrors: showValidationErrors,
            setFormError: setFormError
        };
    }

    global.ErrMessageMapper = { create: create };
})(globalThis);
