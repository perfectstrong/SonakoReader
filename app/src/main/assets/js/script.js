/**
 * Generates a table of contents for your document based on the headings
 *  present. Anchors are injected into the document and the
 *  entries in the table of contents are linked to them. The table of
 *  contents will be generated inside of the first element with the id `toc`.
 * @param {HTMLDOMDocument} documentRef Optional A reference to the document
 *  object. Defaults to `document`.
 * @author Matthew Christopher Kastor-Inare III
 * @version 20130726
 * @example
 * // call this after the page has loaded
 * htmlTableOfContents();
 */
function htmlTableOfContents (documentRef) {
    var documentRef = documentRef || document;
    var toc = documentRef.getElementById('toc');
    var headings = [].slice.call(documentRef.body.querySelectorAll('h1, h2, h3, h4, h5, h6'));
    var headingAutoName = 0;
    headings.forEach(function (heading, index) {
        var link = documentRef.createElement('a');
        if (!heading.id) {
            headingAutoName += 1;
            heading.id = "" + headingAutoName;
        }
        link.setAttribute('href', '#' + heading.id);
        link.textContent = heading.textContent;

        var div = documentRef.createElement('div');
        div.setAttribute('class', "toc-" + heading.tagName.toLowerCase());

        div.appendChild(link);
        toc.appendChild(div);
    });
}
// Add toc
tocWrapper = document.createElement("div");
tocWrapper.id = "toc-wrapper";
document.body.prepend(tocWrapper);

toggler = document.createElement("a");
toggler.classList += "toggle-collapse";
toggler.textContent = "X";
tocWrapper.appendChild(toggler);

tocTitle = document.createElement("p");
tocTitle.classList += "toc-title inactive";
tocTitle.textContent = "M\u1ee5c L\u1ee5c";
tocWrapper.appendChild(tocTitle);

toc = document.createElement("div");
toc.id = "toc";
toc.classList += "inactive";
tocWrapper.appendChild(toc);
htmlTableOfContents();

// Toggler
toggler.onclick = function(e) {
    tocTitle.classList.toggle("inactive");
    toc.classList.toggle("inactive");
}