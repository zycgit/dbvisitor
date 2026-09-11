const path = require('path');

// Keep bookmarks to the former driver manuals working in both locales.
module.exports = function documentRedirects(context) {
    return {
        name: 'document-redirects',
        async contentLoaded({actions}) {
            const redirects = {};
            for (const source of ['milvus', 'mongo', 'elastic', 'redis']) {
                redirects[`drivers/${source}/commands`] = `features/${source}/commands`;
                redirects[`drivers/${source}/usecase`] = `features/${source}/jdbc`;
            }
            redirects['drivers/milvus/compatibility'] = 'features/milvus/compatibility';
            redirects['drivers/elastic/vectors'] = 'features/elastic/vectors';
            for (const [from, to] of Object.entries(redirects)) {
                const target = `${context.baseUrl}docs/${to}`;
                const anchors = {};
                if (from.endsWith('/usecase')) {
                    const connection = `${context.baseUrl}docs/${from.replace('/usecase', '/connection')}`;
                    for (const anchor of ['依赖与版本', 'dependencies-and-versions', '1-版本与依赖', '1-versions-and-dependencies']) {
                        anchors[anchor] = connection.replace('/connection', '/dependencies') + '#' + anchor;
                    }
                    if (from.includes('/mongo/') || from.includes('/elastic/')) {
                        anchors['jdbc-连接'] = connection + '#jdbc-连接';
                        anchors['jdbc-connection'] = connection + '#jdbc-connection';
                    }
                }
                const data = await actions.createData(from.replaceAll('/', '-') + '.json', {target, anchors});
                actions.addRoute({
                    path: `${context.baseUrl}docs/${from}`,
                    exact: true,
                    component: path.resolve(__dirname, '../src/components/DocumentRedirect.js'),
                    modules: {redirect: data},
                });
            }
        },
    };
};
