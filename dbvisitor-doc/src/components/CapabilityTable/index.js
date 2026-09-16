import React from 'react';
import Link from '@docusaurus/Link';
import useDocusaurusContext from '@docusaurus/useDocusaurusContext';
import {useBaseUrlUtils} from '@docusaurus/useBaseUrl';
import useBrokenLinks from '@docusaurus/useBrokenLinks';
import sources from '@site/src/data/capabilities/sources.json';
import styles from './styles.module.css';

const datasourceFiles = require.context('@site/src/data/capabilities/datasources', false, /\.json$/);
const datasourceCapabilities = Object.fromEntries(datasourceFiles.keys().map(file => {
    const datasource = datasourceFiles(file);
    return [datasource.id, datasource.capabilities];
}));

const statuses = {
    supported: {className: 'yes', 'zh-cn': '✓ 支持', en: '✓ Supported'},
    unsupported: {className: 'no', 'zh-cn': '✕ 不支持', en: '✕ Unsupported'},
    partial: {className: 'partial', 'zh-cn': '◐ 部分', en: '◐ Partial'},
    limited: {className: 'partial', 'zh-cn': '◐ 有限', en: '◐ Limited'},
};

function localized(value, locale) {
    return typeof value === 'string' ? value : value[locale];
}

export default function CapabilityTable({matrix, showCounts = true}) {
    const {i18n: {currentLocale}} = useDocusaurusContext();
    const locale = currentLocale === 'en' ? 'en' : 'zh-cn';
    const {withBaseUrl} = useBaseUrlUtils();
    const brokenLinks = useBrokenLinks();
    const columns = matrix.groups.flatMap(group => group.columns.map((column, index) => ({
        ...column,
        key: `${matrix.id}/${group.id}/${column.id}`,
        anchor: index === 0 && group.anchor ? localized(group.anchor, locale) : undefined,
    })));

    // Keep existing section links on each group's first capability column.
    for (const group of matrix.groups) {
        if (group.anchor) {
            brokenLinks.collectAnchor(localized(group.anchor, locale));
        }
    }

    function renderResult(result) {
        const status = statuses[result.status];
        const apiNames = {
            jdbc: {'zh-cn': '编程式 API', en: 'Programmatic API'},
            mapper: {'zh-cn': 'Mapper API（方法注解）', en: 'Mapper API (annotations)'},
            builder: {'zh-cn': '构造器 API', en: 'Builder API'},
            'mapper-file': {'zh-cn': 'Mapper 文件', en: 'Mapper files'},
        };
        const variants = result.variants && result.status !== 'limited'
            ? Object.entries(result.variants).map(([name, value]) => `${apiNames[name]?.[locale] || name}: ${statuses[value.status][locale]}`).join('\n')
            : undefined;
        const label = matrix.id === 'transactions' && variants && result.status === 'supported'
            ? (locale === 'en' ? '✓ All' : '✓ 全部')
            : status[locale];
        const content = (
            <span className={`support-status support-status--${status.className}`} title={variants}>
                {label}{showCounts && result.status === 'partial' ? ` ${result.passed}/${result.total}` : ''}
            </span>
        );
        return result.href
            ? <Link to={withBaseUrl(localized(result.href, locale))}>{content}</Link>
            : content;
    }

    return (
        <table className={styles.matrix} aria-label={locale === 'en' ? 'Data source capabilities' : '数据源能力对照'}>
            <thead>
                <tr>
                    <th scope="col">{locale === 'en' ? 'Data source' : '数据源'}</th>
                    {columns.map(column => (
                        <th key={column.key} scope="col" align="center" id={column.anchor}>
                            <Link to={withBaseUrl(column.href)}>{localized(column.title, locale)}</Link>
                        </th>
                    ))}
                </tr>
            </thead>
            <tbody>
                {sources.map(source => (
                    <tr key={source.id}>
                        <th scope="row"><Link to={withBaseUrl(source.href)}>{source.label}</Link></th>
                        {columns.map(column => (
                            <td key={column.key}>{renderResult(datasourceCapabilities[source.id][column.key])}</td>
                        ))}
                    </tr>
                ))}
            </tbody>
        </table>
    );
}
