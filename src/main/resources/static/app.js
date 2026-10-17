const messages = document.querySelector('#messages');
const input = document.querySelector('#message');
const form = document.querySelector('#form');
const typing = document.querySelector('#typing');

const documentsDialog = document.querySelector('#documents-dialog');

function openDocumentsDialog() {
    if (typeof documentsDialog.showModal === 'function') {
        try {
            documentsDialog.showModal();
            return;
        } catch {
            // Fall back to an open dialog for browsers with partial dialog support.
        }
    }
    documentsDialog.classList.add('fallback');
    documentsDialog.setAttribute('open', '');
}

function closeDocumentsDialog() {
    if (typeof documentsDialog.close === 'function' && documentsDialog.open) {
        documentsDialog.close();
    } else {
        documentsDialog.removeAttribute('open');
    }
    documentsDialog.classList.remove('fallback');
}

document.querySelector('#open-documents').addEventListener('click', async event => {
    event.preventDefault();
    const list = document.querySelector('#documents-list');
    list.replaceChildren();
    openDocumentsDialog();
    try {
        const documents = await get('/api/documents');
        documents.forEach(documentInfo => {
            const section = document.createElement('section');
            section.className = 'document';
            const title = document.createElement('h3');
            title.textContent = documentInfo.name.replace(/\.pdf$/i, '').replace(/[-_]/g, ' ');
            const frame = document.createElement('iframe');
            frame.title = title.textContent;
            frame.src = documentInfo.url;
            section.append(title, frame);
            list.append(section);
        });
        if (!documents.length) {
            list.textContent = 'No support documents are available.';
        }
    } catch {
        list.textContent = 'Support documents could not be loaded. Please try again.';
    }
});

document.querySelector('#close-documents').addEventListener('click', closeDocumentsDialog);
documentsDialog.addEventListener('click', event => {
    if (event.target === documentsDialog) {
        closeDocumentsDialog();
    }
});

input.addEventListener('focus', () => {
    requestAnimationFrame(() => {
        messages.scrollTop = messages.scrollHeight;
        input.scrollIntoView({ behavior: 'smooth', block: 'end' });
    });
});

function bubble(text, speaker, sources = []) {
    const bubbleElement = document.createElement('div');
    bubbleElement.className = 'message ' + speaker;
    bubbleElement.textContent = text;
    if (sources.length) {
        const sourcesElement = document.createElement('div');
        sourcesElement.className = 'sources';
        sourcesElement.textContent = 'Sources: ';
        sources.forEach(source => {
            const sourceElement = document.createElement('span');
            sourceElement.className = 'source';
            sourceElement.textContent = source;
            sourcesElement.append(sourceElement);
        });
        bubbleElement.append(sourcesElement);
    }
    messages.append(bubbleElement);
    messages.scrollTop = messages.scrollHeight;
}

async function get(url, options = {}) {
    const response = await fetch(url, {
        headers: {
            'Content-Type': 'application/json'
        },
        ...options
    });
    return response.json();
}

async function refresh() {
    const [customers, currentUser] = await Promise.all([get('/api/customers'), get('/api/me')]);
    const customerRoot = document.querySelector('#customers');
    customerRoot.replaceChildren(...customers.map(customer => {
        const customerButton = document.createElement('button');
        customerButton.textContent = customer.name;
        customerButton.onclick = async () => {
            await get('/api/login', {
                method: 'POST',
                body: JSON.stringify({
                    customerId: customer.id
                })
            });
            refresh();
        };
        return customerButton;
    }));
    document.querySelector('#me').textContent = currentUser.guest ? 'Guest mode' : `Logged in as ${currentUser.name}`;
}

document.querySelector('#logout').onclick = async () => {
    await get('/api/logout', {
        method: 'POST'
    });
    refresh();
};

document.querySelectorAll('.samples button').forEach(button => {
    button.onclick = () => {
        input.value = button.textContent;
        form.requestSubmit();
    };
});

form.onsubmit = async event => {
    event.preventDefault();
    const message = input.value.trim();
    if (!message) {
        return;
    }
    input.value = '';
    bubble(message, 'user');
    typing.hidden = false;
    try {
        const response = await get('/api/chat', {
            method: 'POST',
            body: JSON.stringify({
                message
            })
        });
        bubble(response.reply || response.error, 'bot', response.sources || []);
    } catch {
        bubble('Sorry, SkyTel support is temporarily unavailable.', 'bot');
    } finally {
        typing.hidden = true;
    }
};

refresh();
