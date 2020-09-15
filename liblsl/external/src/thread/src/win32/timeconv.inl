// Copyright (C) 2001-2003
// William E. Kempf
//
//  Distributed under the Boost Software License, Version 1.0. (See accompanying 
//  file LICENSE_1_0.txt or copy at http://www.lslboost.org/LICENSE_1_0.txt)

// lslboostinspect:nounnamed

namespace {
const int MILLISECONDS_PER_SECOND = 1000;
const int NANOSECONDS_PER_SECOND = 1000000000;
const int NANOSECONDS_PER_MILLISECOND = 1000000;

const int MICROSECONDS_PER_SECOND = 1000000;
const int NANOSECONDS_PER_MICROSECOND = 1000;

inline void to_time(int milliseconds, lslboost::xtime& xt)
{
    int res = 0;
    res = lslboost::xtime_get(&xt, lslboost::TIME_UTC_);
    assert(res == lslboost::TIME_UTC_);

    xt.sec += (milliseconds / MILLISECONDS_PER_SECOND);
    xt.nsec += ((milliseconds % MILLISECONDS_PER_SECOND) *
        NANOSECONDS_PER_MILLISECOND);

    if (xt.nsec >= NANOSECONDS_PER_SECOND)
    {
        ++xt.sec;
        xt.nsec -= NANOSECONDS_PER_SECOND;
    }
}

#if defined(BOOST_HAS_PTHREADS)
inline void to_timespec(const lslboost::xtime& xt, timespec& ts)
{
    ts.tv_sec = static_cast<int>(xt.sec);
    ts.tv_nsec = static_cast<int>(xt.nsec);
    if(ts.tv_nsec >= NANOSECONDS_PER_SECOND)
    {
        ts.tv_sec += ts.tv_nsec / NANOSECONDS_PER_SECOND;
        ts.tv_nsec %= NANOSECONDS_PER_SECOND;
    }
}

inline void to_time(int milliseconds, timespec& ts)
{
    lslboost::xtime xt;
    to_time(milliseconds, xt);
    to_timespec(xt, ts);
}

inline void to_timespec_duration(const lslboost::xtime& xt, timespec& ts)
{
    lslboost::xtime cur;
    int res = 0;
    res = lslboost::xtime_get(&cur, lslboost::TIME_UTC_);
    assert(res == lslboost::TIME_UTC_);

    if (lslboost::xtime_cmp(xt, cur) <= 0)
    {
        ts.tv_sec = 0;
        ts.tv_nsec = 0;
    }
    else
    {
        ts.tv_sec = xt.sec - cur.sec;
        ts.tv_nsec = xt.nsec - cur.nsec;

        if( ts.tv_nsec < 0 )
        {
            ts.tv_sec -= 1;
            ts.tv_nsec += NANOSECONDS_PER_SECOND;
        }
        if(ts.tv_nsec >= NANOSECONDS_PER_SECOND)
        {
            ts.tv_sec += ts.tv_nsec / NANOSECONDS_PER_SECOND;
            ts.tv_nsec %= NANOSECONDS_PER_SECOND;
        }
    }
}
#endif

inline void to_duration(lslboost::xtime xt, int& milliseconds)
{
    lslboost::xtime cur;
    int res = 0;
    res = lslboost::xtime_get(&cur, lslboost::TIME_UTC_);
    assert(res == lslboost::TIME_UTC_);

    if (lslboost::xtime_cmp(xt, cur) <= 0)
        milliseconds = 0;
    else
    {
        if (cur.nsec > xt.nsec)
        {
            xt.nsec += NANOSECONDS_PER_SECOND;
            --xt.sec;
        }
        milliseconds = (int)((xt.sec - cur.sec) * MILLISECONDS_PER_SECOND) +
            (((xt.nsec - cur.nsec) + (NANOSECONDS_PER_MILLISECOND/2)) /
                NANOSECONDS_PER_MILLISECOND);
    }
}

inline void to_microduration(lslboost::xtime xt, int& microseconds)
{
    lslboost::xtime cur;
    int res = 0;
    res = lslboost::xtime_get(&cur, lslboost::TIME_UTC_);
    assert(res == lslboost::TIME_UTC_);

    if (lslboost::xtime_cmp(xt, cur) <= 0)
        microseconds = 0;
    else
    {
        if (cur.nsec > xt.nsec)
        {
            xt.nsec += NANOSECONDS_PER_SECOND;
            --xt.sec;
        }
        microseconds = (int)((xt.sec - cur.sec) * MICROSECONDS_PER_SECOND) +
            (((xt.nsec - cur.nsec) + (NANOSECONDS_PER_MICROSECOND/2)) /
                NANOSECONDS_PER_MICROSECOND);
    }
}
}

// Change Log:
//    1 Jun 01  Initial creation.
