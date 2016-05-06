(function($){
    $(document).ready(function() {
        var leftFileManager = new FileManager($('.left-table'));
        var leftFileManager = new FileManager($('.right-table'));
        setOnTableRowClickHandler();
    });
})(jQuery);

var setOnTableRowClickHandler = function() {
    $('table').on('click', 'tr', function(event) {
        $(this).siblings().find('td').removeClass('bg-info')
        $(this).find('td').addClass('bg-info');
    });
};

var FileManager = (function (FileManager) {

    var $elem;

    var FileManager = function ($elem) {
        this.$elem = $elem;
        var self = this,
            $tbody = $elem.find('tbody');
        
        renderDirectory("");
        
        function renderDirectory(path) {
            var fileNodes = [];
            loadDirectory.call(this, path).done(function (data) {
                $tbody.empty();
                data.subNodes.forEach(function(elem){
                    fileNodes.push(getJqueryObjectByData(elem));
                });
                $elem.parent().find('h4').text(path ? path : 'root');
                $tbody.append(fileNodes);
                $tbody.find('tr').dblclick(onFileNodeDoubleClickHandler);
            });
        }
        
        var onFileNodeDoubleClickHandler = function(event) {
            var currentFileNode = $(event.currentTarget);
            if(currentFileNode.data('directory')) {
                if(currentFileNode.data('filepath')) {
                    renderDirectory(currentFileNode.data('filepath'));
                } else {
                    renderDirectory("");
                }
            }
        };
    };

    var loadDirectory = function (path) {
        var deferred = $.Deferred();
        $.get( "/filemanager/services/explorer/local/tree", { path: path} )
            .done(function( data ) {
                    deferred.resolve(data);
                });   
        return deferred;
    };
    

    var getJqueryObjectByData = function (fileNode) {
        var tr = $('<tr></tr>'),
            name = $('<td class="col-xs-7"></td>'),
            date = $('<td class="col-xs-3"></td>'),
            actions = $('<td class="col-xs-2"> </td>'),
            folder = $('<span class="glyphicon glyphicon-folder-open" aria-hidden="true"></span>'),
            file = $('<span class="glyphicon glyphicon-file" aria-hidden="true"></span>');
        tr.attr('data-directory', fileNode.directory);
        tr.attr('data-filepath', fileNode.filePath);
        date.text(fileNode.fileDate);
        name.append(fileNode.directory ? folder : file);
        name.append("  ", fileNode.fileName ? fileNode.fileName : fileNode.filePath);
        tr.append(name, date, actions.append());
        return tr;
    };

    

    return FileManager;
})(FileManager);