<content:title>
    <fmt:message key="contributions" />
</content:title>

<content:section cssId="contributionsPage">
    <h2><content:gettitle /></h2>

    <div class="card-panel">
        <h5>Most Recent Storybook Contributions</h5>
        <div class="row">
            <c:forEach var="storyBookContributionEvent" items="${storyBookContributionEvents}">
                <c:set var="storyBook" value="${storyBookContributionEvent.storyBook}" />
                <div class="col s12 m6 l4">
                    <a name="${storyBook.id}"></a>
                    <div class="storyBook card">
                        <c:if test="${not empty storyBook.coverImage}">
                            <a href="<spring:url value='/content/storybook/edit/${storyBook.id}' />">
                                <div class="card-image" style="height: 10em; background-image: url(<spring:url value='/image/${storyBook.coverImage.id}_r${storyBook.coverImage.revisionNumber}.${fn:toLowerCase(storyBook.coverImage.imageFormat)}' />);">
                                    <span class="card-title"><c:out value="${storyBook.title}" /></span>
                                </div>
                            </a>
                        </c:if>
                        <div class="card-content">
                            <p class="grey-text" style="margin-bottom: 0.5em;"><c:out value="${storyBook.description}" /></p>
                            <p><fmt:message key="reading.level.${storyBook.readingLevel}" /></p>
                            <p><fmt:message key="revision" />: #${storyBookContributionEvent.revisionNumber}</p>

                            <div class="divider" style="margin: 1em 0;"></div>

                            <a class="editLink" href="<spring:url value='/content/storybook/edit/${storyBook.id}' />"><i class="material-icons">edit</i><fmt:message key="edit" /></a>
                        </div>
                    </div>
                </div>
            </c:forEach>
        </div>
        
        <div class="divider" style="margin: 1.5em 0 2em 0;"></div>
        
        <h5>Most Recent Word Contributions</h5>
        <table class="bordered striped highlight">
            <thead>
                <th><fmt:message key="text" /></th>
                <th><fmt:message key="allophones" /></th>
                <th><fmt:message key="word.type" /></th>
                <th><fmt:message key="revision" /></th>
                <th><fmt:message key="edit" /></th>
            </thead>
            <tbody>
                <c:forEach var="wordContributionEvent" items="${wordContributionEvents}">
                    <c:set var="word" value="${wordContributionEvent.word}" />
                    <tr>
                        <td>
                            <a name="${word.id}"></a>
                            "${word.text}"
                        </td>
                        <td>
                            /<c:forEach var="ltam" items="${word.letterToAllophoneMappings}">&nbsp;<a href="<spring:url value='/content/letter-to-allophone-mapping/edit/${ltam.id}' />"><c:forEach var="allophone" items="${ltam.allophones}">${allophone.valueIpa}</c:forEach></a>&nbsp;</c:forEach>/
                        </td>
                        <td>
                            ${word.wordType}<br />
                            <c:out value=" ${emojisByWordId[word.id]}" />
                        </td>
                        <td>
                            <p>#${wordContributionEvent.revisionNumber}</p>
                        </td>
                        <td><a class="editLink" href="<spring:url value='/content/word/edit/${word.id}' />"><span class="material-icons">edit</span></a></td>
                    </tr>
                </c:forEach>
            </tbody>
        </table>
    </div>
</content:section>

<content:aside>
    <h5 class="center"><fmt:message key="top.contributors" /> 🏆</h5>
    <div class="card-panel deep-purple lighten-5">
        <b><fmt:message key="storybooks" /></b><br />
        <ol style="list-style-type: inherit;">
            <c:forEach var="contributor" items="${contributorsWithStoryBookContributions}">
                <li>
                    <div class="chip">
                        <c:choose>
                            <c:when test="${empty contributor.providerIdWeb3}">
                                <img src="<spring:url value='${contributor.imageUrl}' />" alt="${contributor.firstName}" /> 
                                <c:out value="${contributor.firstName}" />&nbsp;<c:out value="${contributor.lastName}" />
                            </c:when>
                            <c:otherwise>
                                <img src="http://62.75.236.14:3000/identicon/<c:out value="${contributor.providerIdWeb3}" />" />
                                <c:out value="${fn:substring(contributor.providerIdWeb3, 0, 6)}" />&#8230;<c:out value="${fn:substring(contributor.providerIdWeb3, 38, 42)}" />
                            </c:otherwise>
                        </c:choose>
                    </div> (${storyBookContributionsCountMap[contributor.id]})
                </li>
            </c:forEach>
        </ol>
        
        <div class="divider" style="margin: 1em 0;"></div>
        
        <b><fmt:message key="audios" /></b><br />
        <ol style="list-style-type: inherit;">
            <c:forEach var="contributor" items="${contributorsWithAudioContributions}">
                <li>
                    <div class="chip">
                        <c:choose>
                            <c:when test="${empty contributor.providerIdWeb3}">
                                <img src="<spring:url value='${contributor.imageUrl}' />" alt="${contributor.firstName}" /> 
                                <c:out value="${contributor.firstName}" />&nbsp;<c:out value="${contributor.lastName}" />
                            </c:when>
                            <c:otherwise>
                                <img src="http://62.75.236.14:3000/identicon/<c:out value="${contributor.providerIdWeb3}" />" />
                                <c:out value="${fn:substring(contributor.providerIdWeb3, 0, 6)}" />&#8230;<c:out value="${fn:substring(contributor.providerIdWeb3, 38, 42)}" />
                            </c:otherwise>
                        </c:choose>
                    </div> (${audioContributionsCountMap[contributor.id]})
                </li>
            </c:forEach>
        </ol>
        
        <div class="divider" style="margin: 1em 0;"></div>
        
        <b><fmt:message key="words" /></b><br />
        <ol style="list-style-type: inherit;">
            <c:forEach var="contributor" items="${contributorsWithWordContributions}">
                <li>
                    <div class="chip">
                        <c:choose>
                            <c:when test="${empty contributor.providerIdWeb3}">
                                <img src="<spring:url value='${contributor.imageUrl}' />" alt="${contributor.firstName}" /> 
                                <c:out value="${contributor.firstName}" />&nbsp;<c:out value="${contributor.lastName}" />
                            </c:when>
                            <c:otherwise>
                                <img src="http://62.75.236.14:3000/identicon/<c:out value="${contributor.providerIdWeb3}" />" />
                                <c:out value="${fn:substring(contributor.providerIdWeb3, 0, 6)}" />&#8230;<c:out value="${fn:substring(contributor.providerIdWeb3, 38, 42)}" />
                            </c:otherwise>
                        </c:choose>
                    </div> (${wordContributionsCountMap[contributor.id]})
                </li>
            </c:forEach>
        </ol>
        
        <div class="divider" style="margin: 1em 0;"></div>
        
        <b><fmt:message key="numbers" /></b><br />
        <ol style="list-style-type: inherit;">
            <c:forEach var="contributor" items="${contributorsWithNumberContributions}">
                <li>
                    <div class="chip">
                        <c:choose>
                            <c:when test="${empty contributor.providerIdWeb3}">
                                <img src="<spring:url value='${contributor.imageUrl}' />" alt="${contributor.firstName}" /> 
                                <c:out value="${contributor.firstName}" />&nbsp;<c:out value="${contributor.lastName}" />
                            </c:when>
                            <c:otherwise>
                                <img src="http://62.75.236.14:3000/identicon/<c:out value="${contributor.providerIdWeb3}" />" />
                                <c:out value="${fn:substring(contributor.providerIdWeb3, 0, 6)}" />&#8230;<c:out value="${fn:substring(contributor.providerIdWeb3, 38, 42)}" />
                            </c:otherwise>
                        </c:choose>
                    </div> (${numberContributionsCountMap[contributor.id]})
                </li>
            </c:forEach>
        </ol>
    </div>
    
    <div class="divider" style="margin: 1.5em 0;"></div>
    
    <h5 class="center">Token Holders</h5>
    <div class="card-panel deep-purple lighten-5">
        <p>
            Active contributors get rewarded with 
            <c:choose>
                <c:when test="${applicationScope.configProperties['env'] != 'PROD'}">
                    <a href="https://rinkeby.aragon.org/#/elimuai/0xcfc816708740e121dd280969f05cc7e95d977177/" target="_blank">elimu.ai Community Tokens</a>.
                </c:when>
                <c:otherwise>
                    <a href="https://client.aragon.org/#/elimuai/0xee45d21cb426420257bd4a1d9513bcb499ff443a/" target="_blank">elimu.ai Community Tokens</a>.
                </c:otherwise>
            </c:choose>
        </p>
        <p>
            All token holders can participate in the community's <a href="<spring:url value='/contributions/aragon-dao' />">decision making</a>.
        </p>
        <div id="tokenHoldersContainer">
            <div class="progress">
                <div class="indeterminate"></div>
            </div>
            <p>
                Loading...
            </p>
        </div>
        <script>
            /**
             * Copied from AragonRestController.java
             */
            function getBaseUrl() {
                console.info("getBaseUrl")
                let domain = "62.75.236.14"; // DEV/TEST
                <c:if test="${applicationScope.configProperties['env'] == 'PROD'}">
                    domain = "85.93.91.26";
                </c:if>
                return "http://" + domain + ":3000";
            }

            $(function() {
                // Fetch token holders from Aragon Connect (via the REST API)
                $.ajax({
                    dataType: "json",
                    url: "<spring:url value='/rest/v2/aragon/token-holders' />",
                    success: function(tokenHolders) {
                        console.info("success");

                        let htmlString = '<table class="striped">';
                        htmlString += '    <thead>';
                        htmlString += '        <tr>';
                        htmlString += '            <th>Holder</th>';
                        htmlString += '            <th>Balance</th>';
                        htmlString += '        </tr>';
                        htmlString += '    </thead>';
                        htmlString += '    <tbody>';
                        tokenHolders.forEach(function(tokenHolder, index) {
                            htmlString += '<tr>';
                            htmlString += '    <td>';
                            htmlString += '        <div class="chip">';
                            htmlString += '            <img src="' + getBaseUrl() +'/identicon/' + tokenHolder.address + '" />' + tokenHolder.address.substring(0, 6) + "..." + tokenHolder.address.substring(tokenHolder.address.length - 4, tokenHolder.address.length);
                            htmlString += '        </div>';
                            htmlString += '    </td>';
                            htmlString += '    <td>';
                            htmlString += '        ' + tokenHolder.balance/1000000000000000000;
                            htmlString += '    </td>';
                            htmlString += '</tr>';
                        });
                        htmlString += '</tbody>';
                        htmlString += '</table>';
                        $('#tokenHoldersContainer').html(htmlString);
                    }
                });
            });
        </script>
    </div>
</content:aside>
