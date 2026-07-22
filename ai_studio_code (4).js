const firebaseConfig = {
    apiKey: "AIzaSyCkH8ACVHoRxYru1g9oPa9tMD4yBUYQcZM",
    authDomain: "member-reseller-boci.firebaseapp.com",
    projectId: "member-reseller-boci",
    storageBucket: "member-reseller-boci.firebasestorage.app",
    messagingSenderId: "279521008637",
    appId: "1:279521008637:web:0923c9cb51818da7945794"
};

firebase.initializeApp(firebaseConfig);
const auth = firebase.auth();
const db = firebase.firestore();

let currentUser = null;
let catalog = [];
let cart = [];
let currentPointsVal = 0; 

const ping = new Audio("https://assets.mixkit.co/active_storage/sfx/2869/2869-preview.mp3");
let loadOrders = true, loadReturns = true, loadComplaints = true, loadRedeems = true, loadActivations = true;

// --- FITUR BARU: FORMAT TANGGAL ---
function formatDate(timestamp) {
    if (!timestamp) return "-";
    const date = timestamp.toDate();
    return date.toLocaleDateString('id-ID', { day: '2-digit', month: '2-digit', year: 'numeric' });
}

// --- LOGIKA AUTH & CEK AKTIVASI ---
auth.onAuthStateChanged(async (user) => {
    if (user) {
        const doc = await db.collection("users").doc(user.uid).get();
        if (doc.exists) {
            const userData = doc.data();
            if (userData.role !== 'admin' && userData.isActive !== true) {
                alert("Akun belum aktif. Hubungi Admin.");
                auth.signOut(); return;
            }
            currentUser = { id: user.uid, ...userData };
            initApp();
        }
    } else {
        document.getElementById("appWrapper").classList.add("hidden");
        document.getElementById("loginScreen").classList.remove("hidden");
    }
});

function initApp() {
    document.getElementById("loginScreen").classList.add("hidden");
    document.getElementById("appWrapper").classList.remove("hidden");
    document.getElementById("userGreetName").innerText = currentUser.nama || "User";
    renderSidebar();
    syncCatalog();

    if (currentUser.role === 'admin') {
        document.getElementById("adminNotifHeader").classList.remove("hidden");
        document.getElementById("btnTukarPoinHeader").classList.add("hidden");
        showSection('secAdminDashboard');
        loadAdminData();
    } else {
        document.getElementById("adminNotifHeader").classList.add("hidden");
        document.getElementById("btnTukarPoinHeader").classList.remove("hidden");
        showSection('secResellerDashboard');
        loadResellerData();
        loadResellerHistory();
    }
}

// --- LOGIKA DAFTAR ---
document.getElementById("registerForm").onsubmit = async (e) => {
    e.preventDefault();
    const nama = document.getElementById("regNama").value;
    const email = document.getElementById("regEmail").value;
    const pass = document.getElementById("regPassword").value;
    const hp = document.getElementById("regHp").value;
    const cleanNama = nama.replace(/\s/g, '').substring(0, 4).toLowerCase();
    const randomNum = Math.floor(10000 + Math.random() * 90000);
    const customId = cleanNama + randomNum;
    try {
        const cred = await auth.createUserWithEmailAndPassword(email, pass);
        await db.collection("users").doc(cred.user.uid).set({
            customId: customId, nama, email, hp, role: 'reseller', isActive: false, createdAt: firebase.firestore.FieldValue.serverTimestamp()
        });
        alert("Pendaftaran Berhasil! ID: " + customId);
        window.open(`https://wa.me/62895345452412?text=Aktivasi OKTSHOP17 ID: ${customId}`, '_blank');
        auth.signOut(); 
    } catch (err) { alert(err.message); }
};

// --- LOGIKA PESANAN & POIN (PEMBARUAN TABEL & DETAIL) ---
function loadResellerData() {
    db.collection("orders").where("resellerId", "==", currentUser.id).orderBy("createdAt", "desc").onSnapshot(sOrders => {
        db.collection("redemptions").where("resellerId", "==", currentUser.id).where("status", "==", "Selesai").onSnapshot(sRedeems => {
            let q = 0, t = 0;
            document.getElementById("resellerOrderTable").innerHTML = sOrders.docs.map(d => {
                const o = d.data();
                if(o.status === 'Selesai') { q += (o.jumlah || 0); t += (o.total || 0); }
                return `<tr>
                    <td>${formatDate(o.createdAt)}</td>
                    <td>${o.customerName}</td>
                    <td><small>${o.produk}</small></td>
                    <td>Rp ${o.total.toLocaleString('id-ID')}</td>
                    <td><span class="badge-status ${o.status === 'Selesai' ? 'status-selesai' : 'status-proses'}">${o.status}</span></td>
                </tr>`;
            }).join('');
            
            let usedPoints = 0;
            sRedeems.docs.forEach(d => { usedPoints += (d.data().points || 0); });
            currentPointsVal = Math.floor(t / 100) - usedPoints;
            document.getElementById("resQty").innerText = q;
            document.getElementById("resTotal").innerText = "Rp " + t.toLocaleString('id-ID');
            document.getElementById("resPoin").innerText = currentPointsVal.toLocaleString('id-ID');
            document.getElementById("displayMyPoints").innerText = currentPointsVal.toLocaleString('id-ID');
        });
    });
}

// --- RIWAYAT RETUR & KELUHAN (PEMBARUAN TABEL) ---
function loadResellerHistory() {
    db.collection("returns").where("resellerId", "==", currentUser.id).orderBy("createdAt", "desc").onSnapshot(s => {
        document.getElementById("resellerReturnHistory").innerHTML = s.docs.map(doc => {
            const d = doc.data();
            return `<tr>
                <td>${formatDate(d.createdAt)}</td>
                <td><b>${d.produk}</b></td>
                <td><i>${d.alasan}</i></td>
                <td><span class="badge-status ${d.status === 'Selesai' ? 'status-selesai' : 'status-proses'}">${d.status || 'proses'}</span></td>
            </tr>`;
        }).join('');
    });
    db.collection("complaints").where("resellerId", "==", currentUser.id).orderBy("createdAt", "desc").onSnapshot(s => {
        document.getElementById("resellerCompHistory").innerHTML = s.docs.map(doc => {
            const d = doc.data();
            return `<tr>
                <td>${formatDate(d.createdAt)}</td>
                <td>${d.pesan}</td>
                <td><span class="badge-status ${d.status === 'Selesai' ? 'status-selesai' : 'status-proses'}">${d.status || 'proses'}</span></td>
            </tr>`;
        }).join('');
    });
}

// --- LOGIKA ADMIN DATA ---
function loadAdminData() {
    db.collection("users").where("role", "==", "reseller").where("isActive", "==", false).onSnapshot(snap => {
        if (!loadActivations) snap.docChanges().forEach(c => { if(c.type === "added") ping.play().catch(e=>{}); });
        loadActivations = false;
        document.getElementById("badgeActivation").innerText = snap.size;
    });
    db.collection("orders").orderBy("createdAt", "desc").onSnapshot(snap => {
        if (!loadOrders) snap.docChanges().forEach(c => { if(c.type === "added") ping.play().catch(e=>{}); });
        loadOrders = false;
        let q=0, t=0, pending=0;
        document.getElementById("adminOrderTable").innerHTML = snap.docs.map(d => {
            const o = d.data();
            if(o.status === 'Selesai') { q++; t += (o.total || 0); }
            if(o.status === 'pending') pending++;
            return `<tr>
                <td>${formatDate(o.createdAt)}</td>
                <td>${o.resellerName}</td>
                <td>${o.customerName}</td>
                <td>${o.produk}</td>
                <td>${o.status==='pending'?`<button onclick="updateStat('orders','${d.id}')" style="background:#F2A93B; border:none; padding:5px 10px; border-radius:5px; color:white; font-weight:bold; cursor:pointer;">Selesai</button>`:'✅'}</td>
            </tr>`;
        }).join('');
        document.getElementById("badgeOrder").innerText = pending;
        document.getElementById("admQty").innerText = q;
        document.getElementById("admTotal").innerText = "Rp "+t.toLocaleString('id-ID');
        document.getElementById("admPoin").innerText = Math.floor(t/100).toLocaleString('id-ID');
    });
    db.collection("redemptions").orderBy("createdAt", "desc").onSnapshot(snap => {
        document.getElementById("adminRedeemTable").innerHTML = snap.docs.map(d => {
            const r = d.data();
            return `<tr><td><b>${r.resellerName}</b></td><td>${r.redeemName}</td><td>${r.points.toLocaleString()}</td><td>${r.wa}</td><td>${r.status === 'proses' ? `<button onclick="updateStat('redemptions','${d.id}')" class="btn-gold-sm">Selesai</button>` : '✅'}</td></tr>`;
        }).join('');
    });
}

function loadActivationList() {
    db.collection("users").where("role", "==", "reseller").where("isActive", "==", false).onSnapshot(snap => {
        const table = document.getElementById("adminActivationTable");
        if(!table) return;
        table.innerHTML = snap.docs.map(doc => {
            const u = doc.data();
            return `<tr><td><b>${u.customId || '-'}</b></td><td>${u.nama}</td><td>${u.hp}</td><td><button onclick="activateUser('${doc.id}')" class="btn-gold-sm">AKTIFKAN</button></td></tr>`;
        }).join('');
    });
}

// --- KATALOG, CART & ORDER (KONSISTEN) ---
function syncCatalog() {
    db.collection("products").onSnapshot(s => {
        catalog = s.docs.map(d => ({ id: d.id, ...d.data() }));
        const cs = document.getElementById("ordCatSelect");
        if(cs) {
            const cats = [...new Set(catalog.map(p => p.kategori || "Umum"))];
            cs.innerHTML = '<option value="Semua">-- Semua --</option>' + cats.map(c => `<option value="${c}">${c}</option>`).join('');
        }
        filterProductsByCategory();
        if (currentUser.role === 'admin') loadAdminCatalog();
    });
}
function filterProductsByCategory() {
    const cat = document.getElementById("ordCatSelect")?.value || "Semua";
    const ps = document.getElementById("ordProdSelect"); if(!ps) return;
    let f = catalog; if (cat !== "Semua") f = catalog.filter(p => (p.kategori || "Umum") === cat);
    ps.innerHTML = f.map(p => `<option value="${p.id}">${p.nama} - Rp${p.harga.toLocaleString('id-ID')}</option>`).join('');
}
function addToCart() {
    const pid = document.getElementById("ordProdSelect").value;
    const qty = parseInt(document.getElementById("ordQtyInput").value);
    const p = catalog.find(item => item.id === pid);
    if (p && qty > 0) { cart.push({ nama: p.nama, qty, subtotal: p.harga * qty }); renderCart(); }
}
function renderCart() {
    const tb = document.getElementById("cartTableBody"); let t = 0;
    tb.innerHTML = cart.map((item, index) => { t += item.subtotal; return `<tr><td>${item.nama}</td><td>${item.qty}</td><td>Rp ${item.subtotal.toLocaleString('id-ID')}</td><td><button onclick="removeFromCart(${index})">X</button></td></tr>`; }).join('');
    document.getElementById("cartTotalText").innerText = "Total: Rp " + t.toLocaleString('id-ID');
}
function removeFromCart(i) { cart.splice(i, 1); renderCart(); }

document.getElementById("orderFormFinal").onsubmit = async (e) => {
    e.preventDefault();
    if(cart.length === 0) return alert("Keranjang kosong!");
    const cust = document.getElementById("ordCustomer").value, hp = document.getElementById("ordHp").value, pay = document.getElementById("ordPayment").value;
    const total = cart.reduce((s, i) => s + i.subtotal, 0);
    const ringkasan = cart.map(i => `${i.nama} (${i.qty}x)`).join(", ");
    try {
        await db.collection("orders").add({ 
            resellerId: currentUser.id, resellerName: currentUser.nama, customerName: cust, customerHp: hp, produk: ringkasan, total, jumlah: cart.reduce((s, i) => s + i.qty, 0), metode: pay, status: "pending", createdAt: firebase.firestore.FieldValue.serverTimestamp() 
        });
        let pesan = `PESANAN BARU\nReseller: ${currentUser.nama}\nPenerima: ${cust}\nDetail: ${ringkasan}\nTotal: Rp ${total.toLocaleString()}`;
        closeOrderModal(); window.open(`https://wa.me/62895345452412?text=${encodeURIComponent(pesan)}`, '_blank');
        cart = []; 
    } catch(err) { alert(err.message); }
};

// --- NAVIGATION & UI ---
function renderSidebar() {
    const nav = document.getElementById("sidebarNav");
    let menu = '';
    if (currentUser.role === 'admin') {
        menu = `<div class="nav-item" onclick="showSection('secAdminDashboard')">📊 Dashboard Admin</div>
        <div class="nav-item" onclick="showSection('secAdminActivation')">🔑 Aktivasi Akun</div>
        <div class="nav-item" onclick="showSection('secAdminRedeem')">🎁 Penukaran Poin</div>
        <div class="nav-item" onclick="showSection('secAdminCatalog')">📦 Update Katalog</div>
        <div class="nav-item" onclick="showSection('secAdminRankings')">🏆 Peringkat Reseller</div>
        <div class="nav-item" onclick="showSection('secAdminReturn')">📥 Returan Masuk</div>
        <div class="nav-item" onclick="showSection('secAdminComplaint')">📢 Keluhan Masuk</div>`;
    } else {
        menu = `<div class="nav-item" onclick="showSection('secResellerDashboard')">📊 Dashboard Reseller</div>
        <div class="nav-item" onclick="showSection('secResellerReturn')">📦 Retur Barang</div>
        <div class="nav-item" onclick="showSection('secResellerComplaint')">📢 Laporan Keluhan</div>`;
    }
    // FITUR BARU: ID USER DI SIDEBAR
    menu += `<div class="nav-item" onclick="showSection('secProfile')">👤 Profil Akun <small>ID: ${currentUser.customId || '-'}</small></div>`;
    nav.innerHTML = menu;
}

function showSection(id) {
    document.querySelectorAll('.app-section').forEach(s => s.classList.add('hidden'));
    const t = document.getElementById(id); if(t) t.classList.remove('hidden');
    if(id === 'secAdminActivation') loadActivationList();
    if(id === 'secResellerDashboard') loadResellerLeaderboard();
    toggleSidebar(false);
}

// --- FUNGSI PENDUKUNG LAINNYA ---
async function loadResellerLeaderboard() {
    const us = await db.collection("users").where("role", "==", "reseller").get();
    const os = await db.collection("orders").where("status", "==", "Selesai").get();
    const all = os.docs.map(d => d.data());
    let ldb = us.docs.map(u => {
        const total = all.filter(o => o.resellerId === u.id).reduce((s, o) => s + (o.total || 0), 0);
        return { nama: u.data().nama, poin: Math.floor(total / 100) };
    });
    ldb.sort((a, b) => b.poin - a.poin);
    document.getElementById("resellerLeaderboardTable").innerHTML = ldb.slice(0, 10).map((res, i) => `<tr><td>${i+1}</td><td>${res.nama}</td><td>${res.poin.toLocaleString()} Poin</td></tr>`).join('');
}

async function activateUser(uid) { if(confirm("Aktifkan?")) { await db.collection("users").doc(uid).update({ isActive: true }); alert("Aktif!"); } }
function logout() { auth.signOut(); }
function toggleSidebar(f) {
    const s = document.getElementById("sidebar"), o = document.getElementById("sidebarOverlay");
    if(f===false){ s.classList.remove("active"); o.classList.remove("active"); } else { s.classList.add("active"); o.classList.add("active"); }
}
function switchAuth(m) {
    document.getElementById("loginForm").classList.toggle("hidden", m==='register');
    document.getElementById("registerForm").classList.toggle("hidden", m==='login');
    document.getElementById("tLog").classList.toggle("active", m==='login');
    document.getElementById("tReg").classList.toggle("active", m==='register');
}
function openOrderModal() { document.getElementById("orderModal").classList.remove("hidden"); cart = []; renderCart(); goToStep1(); }
function closeOrderModal() { document.getElementById("orderModal").classList.add("hidden"); }
function goToStep2() { if (cart.length === 0) return alert("Pilih produk!"); document.getElementById("orderStep1").classList.add("hidden"); document.getElementById("orderStep2").classList.remove("hidden"); }
function goToStep1() { document.getElementById("orderStep1").classList.remove("hidden"); document.getElementById("orderStep2").classList.add("hidden"); }
function openRedeemModal() { document.getElementById("redeemModal").classList.remove("hidden"); goToRedeemStep1(); }
function closeRedeemModal() { document.getElementById("redeemModal").classList.add("hidden"); }
function goToRedeemStep1() { document.getElementById("redeemStep1").classList.remove("hidden"); document.getElementById("redeemStep2").classList.add("hidden"); }
function goToRedeemStep2() { if(currentPointsVal < document.getElementById("redeemAmountSelect").value) return alert("Poin tidak cukup!"); document.getElementById("redeemStep1").classList.add("hidden"); document.getElementById("redeemStep2").classList.remove("hidden"); }

document.getElementById("loginForm").onsubmit = (e) => { e.preventDefault(); auth.signInWithEmailAndPassword(document.getElementById("loginEmail").value, document.getElementById("loginPassword").value); };
document.getElementById("resellerReturnForm").onsubmit = async (e) => { e.preventDefault(); await db.collection("returns").add({ resellerId: currentUser.id, produk: document.getElementById("retProd").value, alasan: document.getElementById("retReason").value, hp: document.getElementById("retHp").value, status: "proses", createdAt: firebase.firestore.FieldValue.serverTimestamp() }); alert("Terkirim!"); e.target.reset(); };
document.getElementById("resellerComplaintForm").onsubmit = async (e) => { e.preventDefault(); await db.collection("complaints").add({ resellerId: currentUser.id, pesan: document.getElementById("compText").value, status: "proses", createdAt: firebase.firestore.FieldValue.serverTimestamp() }); alert("Terkirim!"); e.target.reset(); };
document.getElementById("formRedeemPoints").onsubmit = async (e) => { e.preventDefault(); await db.collection("redemptions").add({ resellerId: currentUser.id, resellerName: currentUser.nama, points: parseInt(document.getElementById("redeemAmountSelect").value), status: "proses", createdAt: firebase.firestore.FieldValue.serverTimestamp() }); alert("Berhasil!"); closeRedeemModal(); };
async function updateStat(coll, id) { if(confirm("Tandai Selesai?")) await db.collection(coll).doc(id).update({ status: "Selesai" }); }