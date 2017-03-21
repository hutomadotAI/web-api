function activeRightMenu(response) {
    if (response == '1') {
        document.getElementById('page_general').classList.remove('active');
        document.getElementById('page_aiskill').classList.add('active');
        document.getElementById('tab_general').className = '';
        document.getElementById('tab_aiskill').className = ('active');
    }
}