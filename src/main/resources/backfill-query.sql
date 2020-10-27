DECLARE startTimeInclusive TIMESTAMP DEFAULT TIMESTAMP("2020-09-28T15:30:00Z");
DECLARE endTimeExclusive TIMESTAMP DEFAULT TIMESTAMP("2020-09-28T16:00:00Z");

-- eg startTimeInclusive=2020-06-01T00:00:00 and endTimeExclusive=2020-06-02T00:00:00 to get exactly 24 hours data.

SELECT results.*
FROM (SELECT
    -- View-specific factors:
    timestamp_add(timestamp_trunc(event_timestamp, hour), INTERVAL EXTRACT(minute from event_timestamp) - MOD(EXTRACT(minute FROM event_timestamp), 30) minute) as bucket_start,
    range_bucket(attention_time - 1, array[0,1,2,3,4,5,10,15,20,25,30,40,50,60,70,80,90,100,110,120,180,240,300,360,420,480,540,600,900,1200]) AS attention_bucket,
    referrer_significant_site,
    (CASE
         WHEN country_code IN ('GB','US','AU','IN','CA','GNM') THEN country_code
         WHEN country_code IN ('AT','BE','BG','HR','CY','CZ','DK','EE','FI','FR','DE','GR','HU','IE','IT','LV','LT','LU','MT','NL','PL','PT','RO','SK','SI','ES','SE') THEN 'EU27'
         WHEN country_code IS NOT NULL THEN 'ROW'
      END) AS rollup_country_code,
    (CASE
         WHEN device_type IN ('GUARDIAN_WINDOWS_APP', 'GUARDIAN_IOS_NATIVE_APP', 'GUARDIAN_ANDROID_NATIVE_APP') THEN 'NonWeb'
         WHEN platform IN ('ANDROID_NATIVE_APP','IOS_NATIVE_APP','WINDOWS_NATIVE_APP', 'AMAZON_ECHO','EDITIONS', 'APPLE_NEWS') THEN 'NonWeb'
         ELSE 'Web'
      END) AS renderer_type,
    (CASE
         WHEN device_type IN ('GUARDIAN_WINDOWS_APP', 'GUARDIAN_IOS_NATIVE_APP', 'GUARDIAN_ANDROID_NATIVE_APP') THEN 'GuardianNativeApp'
         WHEN platform = 'EDITIONS' THEN 'GuardianNativeApp'
         WHEN platform = 'APPLE_NEWS' THEN 'ThirdPartyApp'
      END) AS non_web_renderer,
    (CASE
         WHEN device_type IN ('GUARDIAN_WINDOWS_APP', 'GUARDIAN_IOS_NATIVE_APP', 'GUARDIAN_ANDROID_NATIVE_APP') THEN 'Live'
         WHEN platform = 'EDITIONS' THEN 'Editions'
      END) AS guardian_native_app_family,
    (CASE
         WHEN platform = 'APPLE_NEWS' THEN 'AppleNews'
      END) AS third_party_renderer,
    (CASE
         WHEN device_type IN ('GUARDIAN_WINDOWS_APP', 'GUARDIAN_IOS_NATIVE_APP', 'GUARDIAN_ANDROID_NATIVE_APP', 'SMARTPHONE', 'TABLET') THEN true
         WHEN device_type IS NOT NULL THEN false
      END) AS is_mobile,
    (CASE device_type
         WHEN 'SMARTPHONE' THEN 'Phone'
         WHEN 'TABLET' THEN 'Tablet'
      END) AS mobile_class,
    -- Content-specific factors:
    path,
    -- Bucket count
    count(*) AS count
    FROM datalake.pageview
    WHERE
    received_date between date(startTimeInclusive) and DATE_ADD(date(endTimeExclusive), INTERVAL 7 DAY) -- events can be received much later than they occur. Restricting received_date is valuable because it constrains the amount of data loaded by BigQuery
    GROUP BY 1,2,3,4,5,6,7,8,9,10,11
) AS results
where
startTimeInclusive <= results.bucket_start and results.bucket_start < endTimeExclusive
ORDER BY results.bucket_start, results.path
