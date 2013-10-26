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
    page.viewportSize = { width: 841, height: 595 };
    page.paperSize = {
        format: 'A4',
        orientation: 'landscape',
        //width: "1169px"  ,
        //height: "826px" , 
        //margin: "10px 36px 10px 72px",
        margin: {left:"0px" ,right: "0cm",top:"0cm",bottom    :"0.5cm" },
        border: "0cm",
        /* default header/footer for pages that don't have custom overwrites (see below) */
        header: {
            height: "55px",
            border: "0cm",
            contents: phantom.callback(function(pageNum, numPages) {
                if (pageNum == 1 ) {
                    return "";
                }
                return "<div style='margin-left:44px;margin-top:10px;background-color:#00a651;width:918px;height:10px'  />";
            })
        },
         footer: {
             height: "0.5cm",
 
	     contents: phantom.callback(function(pageNum, numPages) {
                if (pageNum == 1) {
                    return "";
                }
                return "<div style='float:right;margin-right:80px' >" + pageNum + "</div>";
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
