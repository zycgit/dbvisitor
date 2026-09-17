import useDocusaurusContext from '@docusaurus/useDocusaurusContext';
import {useDateTimeFormat} from '@docusaurus/theme-common/internal';

// Use UTC consistently with blog metadata, with zero-padded months and days.
export default function useBlogDateFormat(includeYear = true) {
    const {i18n: {currentLocale}} = useDocusaurusContext();
    const formatter = useDateTimeFormat({
        ...(includeYear ? {year: 'numeric'} : {}),
        month: '2-digit', day: '2-digit', timeZone: 'UTC',
    });
    return date => {
        const value = new Date(date);
        if (currentLocale.startsWith('zh')) {
            const parts = Object.fromEntries(formatter.formatToParts(value)
                .map(({type, value: part}) => [type, part]));
            return `${includeYear ? `${parts.year}年` : ''}${parts.month}月${parts.day}日`;
        }
        return formatter.format(value);
    };
}
