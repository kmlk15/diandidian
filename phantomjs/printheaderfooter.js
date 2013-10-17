var page = require('webpage').create(),
    system = require('system');

function someCallback(pageNum, numPages) {
    return "<h1> someCallback: " + pageNum + " / " + numPages + "</h1>";
}

if (system.args.length < 3) {
    console.log('Usage: printheaderfooter.js URL filename');
    phantom.exit(1);
} else {
    var address = system.args[1];
    var output = system.args[2];
    page.viewportSize = { width: 800, height: 600 };
    page.paperSize = {
        format: 'A4',
        orientation: 'landscape',
        margin: "0cm",
        border: "0cm",
        /* default header/footer for pages that don't have custom overwrites (see below) */
        header: {
            height: "0.5cm",
            left: "-5px",
            contents: phantom.callback(function(pageNum, numPages) {
                if (pageNum == 1) {
                    return "";
                }
                return "<div style='background-color:#00a651;width:120%;height:10px'  />";
            })
        }
    };
    page.open(address, function (status) {
        if (status !== 'success') {
            console.log('Unable to load the address!');
        } else {
            /* check whether the loaded page overwrites the header/footer setting,
               i.e. whether a PhantomJSPriting object exists. Use that then instead
               of our defaults above.

               example:
               <html>
                 <head>
                   <script type="text/javascript">
                     var PhantomJSPrinting = {
                        header: {
                            height: "1cm",
                            contents: function(pageNum, numPages) { return pageNum + "/" + numPages; }
                        },
                        footer: {
                            height: "1cm",
                            contents: function(pageNum, numPages) { return pageNum + "/" + numPages; }
                        }
                     };
                   </script>
                 </head>
                 <body><h1>asdfadsf</h1><p>asdfadsfycvx</p></body>
              </html>
            */
            if (page.evaluate(function(){return typeof PhantomJSPrinting == "object";})) {
                paperSize = page.paperSize;
                paperSize.header.height = page.evaluate(function() {
                    return PhantomJSPrinting.header.height;
                });
                paperSize.header.contents = phantom.callback(function(pageNum, numPages) {
                    return page.evaluate(function(pageNum, numPages){return PhantomJSPrinting.header.contents(pageNum, numPages);}, pageNum, numPages);
                });
                paperSize.footer.height = page.evaluate(function() {
                    return PhantomJSPrinting.footer.height;
                });
                paperSize.footer.contents = phantom.callback(function(pageNum, numPages) {
                    return page.evaluate(function(pageNum, numPages){return PhantomJSPrinting.footer.contents(pageNum, numPages);}, pageNum, numPages);
                });
                page.paperSize = paperSize;
                console.log(page.paperSize.header.height);
                console.log(page.paperSize.footer.height);
            }
            window.setTimeout(function () {
                page.render(output);
                phantom.exit();
            }, 200);
        }
    });
}
