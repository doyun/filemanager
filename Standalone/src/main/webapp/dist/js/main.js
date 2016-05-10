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
                $tbody.find('.glyphicon-download-alt').click(onDownloadFileNodeClickHandler);
                $tbody.find('.glyphicon-trash').click(onDeleteFileNodeClickHandler);
            });
        }
        
        function onFileNodeDoubleClickHandler(event) {
            var currentFileNode = $(event.currentTarget);
            if(currentFileNode.data('directory')) {
                if(currentFileNode.data('filepath')) {
                    renderDirectory(currentFileNode.data('filepath'));
                } else {
                    renderDirectory("");
                }
            }
        }
        
        function onDeleteFileNodeClickHandler(event) {
            var $currentFileNode = $(event.currentTarget).closest('tr');
            confirmAction("you want to delete file " + $currentFileNode.data('filepath'), function() {
                if($currentFileNode.data('filepath')) {
                    deleteFileNode($currentFileNode.data('filepath'))
                        .done(function(data){
                            $currentFileNode.remove();
                        })
                        .fail(function(data){
                            showInformPopup("Unable to delete node. " + data.statusText + ".");
                        });
                }
            });
        }
        
        function onDownloadFileNodeClickHandler(event) {
            var $currentFileNode = $(event.currentTarget).closest('tr');
            confirmAction("you want to download " + $currentFileNode.data('filename'), function() {
                if(!$currentFileNode.data('directory')) {
                    if($currentFileNode.data('filepath')) {
                        downloadFileNode($currentFileNode.data('filepath'))
                            .fail(function(data){
                                alert("fail");
                            });
                    }
                }
            });
        }
    };
    
    var confirmAction = function(text, action) {
        var bodyText = "Are you sure " + text + "?"
        $('.confirm-action-button').off('click').click(action);
        $('#confirmation-popup .confirmation-popup-body-text').text(bodyText);
        $('#confirmation-popup').modal('show');
    };
    
    var showInformPopup = function(text) {
        $('#information-popup .information-popup-body-text').text(text);
        $('#information-popup').modal('show');
    };

    var loadDirectory = function (path) {
        var deferred = $.Deferred();
        $.get( "/filemanager/services/explorer/local/tree", { path: path} )
            .done(function( data ) {
                    deferred.resolve(data);
                });   
        return deferred;
    };
    
    var downloadFileNode = function (path) {
        var deferred = $.Deferred();
        $.fileDownload('/filemanager/services/dao/lfao?filepath=' + path)
            .done(function (data) { deferred.resolve(data); })
            .fail(function (data) { deferred.reject(data); });
        return deferred;
    };
    
    var deleteFileNode = function (path) {
        var deferred = $.Deferred();
        $.ajax({
            url: '/filemanager/services/dao/lfao?filepath=' + path,
            type: 'DELETE',
            success: function(data) {
                deferred.resolve(data);
            },
            error: function(data) {
                deferred.reject(data)
            }
        });  
        return deferred;
    };
    
    var getJqueryObjectByData = function (fileNode) {
        var tr = $('<tr></tr>'),
            name = $('<td class="col-xs-7"></td>'),
            date = $('<td class="col-xs-3"></td>'),
            actions = $('<td class="col-xs-2 actions"> </td>'),
            folder = $('<span class="glyphicon glyphicon-folder-open" aria-hidden="true"></span>'),
            file = $('<span class="glyphicon glyphicon-file" aria-hidden="true"></span>'),
            deleteButton = $('<span class="glyphicon glyphicon-trash" aria-hidden="true"></span>'),
            downloadButton = $('<span class="glyphicon glyphicon-download-alt" aria-hidden="true"></span>');
        tr.attr('data-directory', fileNode.directory);
        tr.attr('data-filename', fileNode.fileName);
        tr.attr('data-filepath', fileNode.filePath);
        date.text(fileNode.fileDate);
        name.append(fileNode.directory ? folder : file);
        name.append(fileNode.fileName ? fileNode.fileName : fileNode.filePath);
        tr.append(name, date, actions.append(deleteButton, fileNode.directory ? '' : downloadButton));
        return tr;
    };

    

    return FileManager;
})(FileManager);