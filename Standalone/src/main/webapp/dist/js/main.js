$('table').on('click', 'tr', function(event) {
    $(this).siblings().find('td').removeClass('bg-info')
    $(this).find('td').addClass('bg-info');
});