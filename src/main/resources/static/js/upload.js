let countdown = 30;
let countdownInterval;

function handleDragOver(e) {
    e.preventDefault();
    e.stopPropagation();
    document.getElementById('dropzone').classList.add('drag-active');
}

function handleDragLeave(e) {
    e.preventDefault();
    e.stopPropagation();
    document.getElementById('dropzone').classList.remove('drag-active');
}

function handleDrop(e) {
    e.preventDefault();
    e.stopPropagation();
    document.getElementById('dropzone').classList.remove('drag-active');
    
    const files = e.dataTransfer.files;
    if (files.length) {
        document.getElementById('fileInput').files = files;
        showFileName(document.getElementById('fileInput'));
    }
}

function showFileName(input) {
    const fileNameDiv = document.getElementById('fileName');
    if (input.files.length) {
        fileNameDiv.textContent = input.files[0].name;
        fileNameDiv.classList.remove('hidden');
    } else {
        fileNameDiv.classList.add('hidden');
    }
}

// Form submission handling
document.addEventListener('DOMContentLoaded', function() {
    document.querySelector('form').addEventListener('submit', function(e) {
        const loadingState = document.getElementById('loadingState');
        loadingState.classList.remove('hidden');
        document.getElementById('dropzone').classList.add('opacity-50', 'pointer-events-none');
    });

    // Start countdown if there's an error
    if (document.getElementById('retryButton')) {
        startCountdown();
    }
});

// Retry functionality
function startCountdown() {
    const retryButton = document.getElementById('retryButton');
    const countdownSpan = document.getElementById('countdown');
    
    retryButton.disabled = true;
    countdown = 30;
    
    countdownInterval = setInterval(() => {
        countdown--;
        countdownSpan.textContent = countdown;
        
        if (countdown <= 0) {
            clearInterval(countdownInterval);
            retryButton.disabled = false;
            document.getElementById('retryText').textContent = 'Try Again';
        }
    }, 1000);
}

function retrySubmission() {
    const form = document.querySelector('form');
    const loadingState = document.getElementById('loadingState');
    const dropzone = document.getElementById('dropzone');
    
    // Reset error state
    const errorDiv = document.querySelector('[th\\:if="${error}"]');
    if (errorDiv) {
        errorDiv.style.display = 'none';
    }
    
    // Show loading state
    loadingState.classList.remove('hidden');
    dropzone.classList.add('opacity-50', 'pointer-events-none');
    
    // Submit the form
    form.submit();
}
