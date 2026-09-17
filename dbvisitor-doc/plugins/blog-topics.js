const fs = require('node:fs/promises');
const path = require('node:path');
const yaml = require('js-yaml');
const {normalizeUrl, getPluginI18nPath} = require('@docusaurus/utils');

async function readTopics(file) {
    let source;
    try {
        source = await fs.readFile(file, 'utf8');
    } catch (error) {
        if (error.code === 'ENOENT') return {};
        throw error;
    }
    const definitions = yaml.load(source, {filename: file}) ?? {};
    if (typeof definitions !== 'object' || Array.isArray(definitions)) {
        throw new Error(file + ': expected a map of topic IDs to labels and descriptions.');
    }
    for (const [id, topic] of Object.entries(definitions)) {
        if (!/^[a-z0-9]+(?:-[a-z0-9]+)*$/.test(id) || !topic || typeof topic.label !== 'string' || !topic.label.trim()
            || (topic.description !== undefined && typeof topic.description !== 'string')) {
            throw new Error(file + ': invalid topic "' + id + '"; use a URL-safe ID, a label and an optional description.');
        }
    }
    return definitions;
}

// Articles declare membership in front matter; YAML files only define the columns.
module.exports = function blogTopics(context) {
    const definitionsPath = path.join(context.siteDir, 'blog/topics.yml');
    const localizedPath = path.join(getPluginI18nPath({
        localizationDir: context.localizationDir,
        pluginName: 'docusaurus-plugin-content-blog',
        pluginId: 'default',
    }), 'topics.yml');
    return {
        name: 'blog-topics',
        getPathsToWatch() {
            return [definitionsPath, localizedPath];
        },
        async loadContent() {
            const definitions = await readTopics(definitionsPath);
            const localized = await readTopics(localizedPath);
            for (const [id, translation] of Object.entries(localized)) {
                if (!Object.hasOwn(definitions, id)) {
                    throw new Error(localizedPath + ': topic "' + id + '" is not defined in ' + definitionsPath);
                }
                definitions[id] = {...definitions[id], ...translation};
            }
            return definitions;
        },
        async allContentLoaded({allContent, actions}) {
            const definitions = allContent['blog-topics'].default;
            const {blogPosts, blogTags = {}} = allContent['docusaurus-plugin-content-blog'].default;
            const topics = new Map(Object.entries(definitions).map(([id, definition]) => [id, {
                id, title: definition.label, description: definition.description || '', posts: [],
            }]));
            for (const {metadata} of blogPosts) {
                if (metadata.unlisted) continue;
                const membership = metadata.frontMatter.topics ?? [];
                const ids = typeof membership === 'string' ? [membership] : membership;
                const source = metadata.source || metadata.permalink;
                if (!Array.isArray(ids) || ids.some(id => typeof id !== 'string')) {
                    throw new Error(source + ': topics must be a topic ID or an array of topic IDs.');
                }
                const post = {
                    title: metadata.title, description: metadata.description,
                    permalink: metadata.permalink, date: metadata.date,
                };
                for (const id of new Set(ids)) {
                    if (!topics.has(id)) {
                        throw new Error(source + ': unknown topic "' + id + '". Define it in ' + definitionsPath);
                    }
                    topics.get(id).posts.push(post);
                }
            }

            const summaries = [];
            for (const topic of topics.values()) {
                if (!topic.posts.length) continue;
                topic.posts.sort((left, right) => Date.parse(right.date) - Date.parse(left.date));
                const permalink = normalizeUrl([context.siteConfig.baseUrl, 'blog/topics', topic.id]);
                const count = topic.posts.length;
                const data = await actions.createData('topic-' + topic.id + '.json', JSON.stringify({...topic, permalink, count}));
                actions.addRoute({
                    path: permalink, exact: true,
                    component: '@site/src/components/BlogTopicPage', modules: {topic: data},
                });
                summaries.push({...topic, permalink, count, posts: topic.posts.slice(0, 6)});
            }

            // Match the blog plugin's public tag index, including localized routes and counts.
            const tags = Object.values(blogTags).filter(tag => !tag.unlisted).map(tag => ({
                label: tag.label, permalink: tag.permalink,
                description: tag.description, count: tag.items.length,
            }));
            actions.setGlobalData({topics: summaries, tags});
        },
    };
};
