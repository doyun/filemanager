(function($){
    $(document).ready(function() {
        var leftFileManager = new FileManager($('.left-table'), ENDPOINTS.LOCAL);
        var rightFileManager = new FileManager($('.right-table'), ENDPOINTS.LOCAL);
        setOnTableRowClickHandler();
        
    });
})(jQuery);

var setOnTableRowClickHandler = function() {
    $('table').on('click', 'tr', function(event) {
        $(this).siblings().find('td').removeClass('bg-info')
        $(this).find('td').addClass('bg-info');
    });
};

var ENDPOINTS = {
    LOCAL : {
        NAME : "local:",
        TREE : "/filemanager/services/explorer/local/tree",
        FAO : "/filemanager/services/dao/lfao"
    },
    FTP : {
        NAME : "ftp:",
        TREE : "/filemanager/services/explorer/ftp/tree",
        FAO : "/filemanager/services/dao/ftpfao"
    }
}

var FileManager = (function (FileManager) {

    var FileManager = function ($elem, _ENDPOINT) {
        this.$elem = $elem;
        this.ENDPOINT = _ENDPOINT;
        var self = this,
            $tbody = $elem.find('tbody');
        
        renderDirectory("");
        setUpDropdown();
        setOnUploadFormSubmitHandler();
        setOnUploadFileSelectHandler();
        $elem.parent().find('#fileUploadForm').attr('action', self.ENDPOINT.FAO);
        
        function renderDirectory(path) {
            var fileNodes = [];
            loadDirectory.call(self, path).done(function (data) {
                $tbody.empty();
                data.subNodes.forEach(function(elem){
                    fileNodes.push(getJqueryFileNodeObjectByData(elem));
                });
                $elem.parent().find('h4').text(self.ENDPOINT.NAME + (path ? path : 'root'));
                $elem.siblings().find('#filepath').val(path ? path : '')
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
                    deleteFileNode.call(self, $currentFileNode.data('filepath'))
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
                        downloadFileNode.call(self, $currentFileNode.data('filepath'))
                            .fail(function(data){
                                //alert("fail");
                            });
                    }
                }
            });
        }
        
        function setOnUploadFormSubmitHandler() {
            $elem.parent().find('#fileUploadForm').ajaxForm({
                success : function (response) {
                    showInformPopup("File uploaded")
                }
            });
        }
        
        function setOnUploadFileSelectHandler() {
            $elem.parent().find("#file").change(function () {
                $elem.parent().find("#filePath").val(this.value);
            });
        }
        
        function setUpDropdown(){
            var container = $elem.parent().find(".dropdown-menu")
            for (var endpoint in ENDPOINTS) {
                if (ENDPOINTS.hasOwnProperty(endpoint)) {
                    var element = ENDPOINTS[endpoint];
                    var item = $("<li data-storage='" + endpoint + "'><a href='#'>" + element.NAME + "</a></li>");
                    item.click(function(event){
                        self.ENDPOINT = ENDPOINTS[$(event.currentTarget).data('storage')];
                        renderDirectory("");
                        $elem.parent().find('#fileUploadForm').attr('action', self.ENDPOINT.FAO);
                    });
                    container.append(item);
                }
            }
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
        $.get( this.ENDPOINT.TREE, { path: path} )
            .done(function( data ) {
                    deferred.resolve(data);
                });   
        return deferred;
    };
    
    var downloadFileNode = function (path) {
        var deferred = $.Deferred();
        $.fileDownload( this.ENDPOINT.FAO + '?filepath=' + path)
            .done(function (data) { deferred.resolve(data); })
            .fail(function (data) { deferred.reject(data); });
        return deferred;
    };
    
    var deleteFileNode = function (path) {
        var deferred = $.Deferred();
        $.ajax({
            url: this.ENDPOINT.FAO + '?filepath=' + path,
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
    
    var getJqueryFileNodeObjectByData = function (fileNode) {
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