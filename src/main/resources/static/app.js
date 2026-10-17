const messages = document.querySelector('#messages'),
    input = document.querySelector('#message'),
    form = document.querySelector('#form'),
    typing = document.querySelector('#typing');

const documentsDialog = document.querySelector('#documents-dialog');
document.querySelector('#open-documents').addEventListener('click', async event => {
    event.preventDefault();
    const list = document.querySelector('#documents-list');
    list.replaceChildren();
    documentsDialog.showModal();
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
        if (!documents.length) list.textContent = 'No support documents are available.';
    } catch {
        list.textContent = 'Support documents could not be loaded. Please try again.';
    }
});
document.querySelector('#close-documents').addEventListener('click', () => documentsDialog.close());
documentsDialog.addEventListener('click', event => {
    if (event.target === documentsDialog) documentsDialog.close();
});

input.addEventListener('focus', () => {
    requestAnimationFrame(() => {
        messages.scrollTop = messages.scrollHeight;
        input.scrollIntoView({ behavior: 'smooth', block: 'end' });
    });
});

function bubble(text, who, sources = []) {
    const d = document.createElement('div');
    d.className = 'message ' + who;
    d.textContent = text;
    if (sources.length) {
        const s = document.createElement('div');
        s.className = 'sources';
        s.textContent = 'Sources: ';
        sources.forEach(x => {
            const c = document.createElement('span');
            c.className = 'source';
            c.textContent = x;
            s.append(c)
        });
        d.append(s)
    }
    messages.append(d);
    messages.scrollTop = messages.scrollHeight
}
async function get(url, opts = {}) {
    const r = await fetch(url, {
        headers: {
            'Content-Type': 'application/json'
        },
        ...opts
    });
    return r.json()
}
async function refresh() {
    const [cs, me] = await Promise.all([get('/api/customers'), get('/api/me')]);
    const root = document.querySelector('#customers');
    root.replaceChildren(...cs.map(c => {
        const b = document.createElement('button');
        b.textContent = c.name;
        b.onclick = async () => {
            await get('/api/login', {
                method: 'POST',
                body: JSON.stringify({
                    customerId: c.id
                })
            });
            refresh()
        };
        return b
    }));
    document.querySelector('#me').textContent = me.guest ? 'Guest mode' : `Logged in as ${me.name}`
}
document.querySelector('#logout').onclick = async () => {
    await get('/api/logout', {
        method: 'POST'
    });
    refresh()
};
document.querySelectorAll('.samples button').forEach(b => b.onclick = () => {
    input.value = b.textContent;
    form.requestSubmit()
});
form.onsubmit = async e => {
    e.preventDefault();
    const message = input.value.trim();
    if (!message) return;
    input.value = '';
    bubble(message, 'user');
    typing.hidden = false;
    try {
        const r = await get('/api/chat', {
            method: 'POST',
            body: JSON.stringify({
                message
            })
        });
        bubble(r.reply || r.error, 'bot', r.sources || [])
    } catch {
        bubble('Sorry, SkyTel support is temporarily unavailable.', 'bot')
    } finally {
        typing.hidden = true
    }
};
refresh();
