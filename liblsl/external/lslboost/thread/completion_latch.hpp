// Distributed under the Boost Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.lslboost.org/LICENSE_1_0.txt)
// (C) Copyright 2013 Vicente J. Botet Escriba

#ifndef BOOST_THREAD_COMPLETION_LATCH_HPP
#define BOOST_THREAD_COMPLETION_LATCH_HPP

#include <lslboost/thread/detail/config.hpp>
#include <lslboost/thread/detail/delete.hpp>
#include <lslboost/thread/detail/counter.hpp>

#include <lslboost/thread/mutex.hpp>
#include <lslboost/thread/lock_types.hpp>
#include <lslboost/thread/condition_variable.hpp>
#include <lslboost/chrono/duration.hpp>
#include <lslboost/chrono/time_point.hpp>
#include <lslboost/assert.hpp>
#ifdef BOOST_NO_CXX11_HDR_FUNCTIONAL
#include <lslboost/function.hpp>
#else
#include <functional>
#endif
//#include <lslboost/thread/latch.hpp>

#include <lslboost/config/abi_prefix.hpp>

namespace lslboost
{
  namespace thread_detail
  {
    void noop()
    {
    }
  }
  class completion_latch
  {
  public:
    /// the implementation defined completion function type
#ifdef BOOST_NO_CXX11_HDR_FUNCTIONAL
    typedef function<void()> completion_function;
#else
    typedef std::function<void()> completion_function;
#endif
    /// noop completion function factory
    static completion_function noop()
    {
      return completion_function(&thread_detail::noop);
    }

  private:
    struct around_wait;
    friend struct around_wait;
    struct around_wait
    {
      completion_latch &that_;
      lslboost::unique_lock<lslboost::mutex> &lk_;
      around_wait(completion_latch &that, lslboost::unique_lock<lslboost::mutex> &lk)
      : that_(that), lk_(lk)
      {
        that_.leavers_.cond_.wait(lk, detail::counter_is_zero(that_.leavers_));
        that_.waiters_.inc_and_notify_all();
        that_.leavers_.cond_.wait(lk, detail::counter_is_not_zero(that_.leavers_));
      }
      ~around_wait()
      {
        that_.waiters_.dec_and_notify_all();
      }
    };

    bool count_down(unique_lock<mutex> &lk)
    {
      BOOST_ASSERT(count_ > 0);
      if (--count_ == 0)
      {
        waiters_.cond_.wait(lk, detail::counter_is_not_zero(waiters_));
        leavers_.assign_and_notify_all(waiters_);
        count_.cond_.notify_all();
        waiters_.cond_.wait(lk, detail::counter_is_zero(waiters_));
        leavers_.assign_and_notify_all(0);
        lk.unlock();
        funct_();
        return true;
      }
      return false;
    }

  public:
    BOOST_THREAD_NO_COPYABLE( completion_latch )

    /// Constructs a latch with a given count.
    completion_latch(std::size_t count) :
      count_(count), funct_(noop()), waiters_(0), leavers_(0)
    {
    }

    /// Constructs a latch with a given count and a completion function.
    template <typename F>
    completion_latch(std::size_t count, BOOST_THREAD_RV_REF(F) funct) :
    count_(count),
    funct_(lslboost::move(funct)),
    waiters_(0),
    leavers_(0)
    {
    }
    template <typename F>
    completion_latch(std::size_t count, void(*funct)()) :
      count_(count), funct_(funct), waiters_(0), leavers_(0)
    {
    }

    ///
    ~completion_latch()
    {
    }

    /// Blocks until the latch has counted down to zero.
    void wait()
    {
      lslboost::unique_lock<lslboost::mutex> lk(mutex_);
      around_wait aw(*this, lk);
      count_.cond_.wait(lk, detail::counter_is_zero(count_));
    }

    /// @return true if the internal counter is already 0, false otherwise
    bool try_wait()
    {
      lslboost::unique_lock<lslboost::mutex> lk(mutex_);
      around_wait aw(*this, lk);
      return (count_ == 0);
    }

    /// try to wait for a specified amount of time
    /// @return whether there is a timeout or not.
    template <class Rep, class Period>
    cv_status wait_for(const chrono::duration<Rep, Period>& rel_time)
    {
      lslboost::unique_lock<lslboost::mutex> lk(mutex_);
      around_wait aw(*this, lk);
      return count_.cond_.wait_for(lk, rel_time, detail::counter_is_zero(count_))
              ? cv_status::no_timeout
              : cv_status::timeout;
    }

    /// try to wait until the specified time_point is reached
    /// @return whether there is a timeout or not.
    template <class Clock, class Duration>
    cv_status wait_until(const chrono::time_point<Clock, Duration>& abs_time)
    {
      lslboost::unique_lock<lslboost::mutex> lk(mutex_);
      around_wait aw(*this, lk);
      return count_.cond_.wait_until(lk, abs_time, detail::counter_is_zero(count_))
          ? cv_status::no_timeout
          : cv_status::timeout;
    }

    /// Decrement the count and notify anyone waiting if we reach zero.
    /// @Requires count must be greater than 0
    void count_down()
    {
      unique_lock<mutex> lk(mutex_);
      count_down(lk);
    }
    void signal()
    {
      count_down();
    }

    /// Decrement the count and notify anyone waiting if we reach zero.
    /// Blocks until the latch has counted down to zero.
    /// @Requires count must be greater than 0
    void count_down_and_wait()
    {
      lslboost::unique_lock<lslboost::mutex> lk(mutex_);
      if (count_down(lk))
      {
        return;
      }
      around_wait aw(*this, lk);
      count_.cond_.wait(lk, detail::counter_is_zero(count_));
    }
    void sync()
    {
      count_down_and_wait();
    }

    /// Reset the counter
    /// #Requires This method may only be invoked when there are no other threads currently inside the count_down_and_wait() method.
    void reset(std::size_t count)
    {
      lslboost::lock_guard<lslboost::mutex> lk(mutex_);
      //BOOST_ASSERT(count_ == 0);
      count_ = count;
    }

    /// Resets the latch with the new completion function.
    /// The next time the internal count reaches 0, this function will be invoked.
    /// This completion function may only be invoked when there are no other threads
    /// currently inside the count_down and wait related functions.
    /// It may also be invoked from within the registered completion function.
    /// @Returns the old completion function if any or noop if

#ifdef BOOST_NO_CXX11_HDR_FUNCTIONAL
    template <typename F>
    completion_function then(BOOST_THREAD_RV_REF(F) funct)
    {
      lslboost::lock_guard<lslboost::mutex> lk(mutex_);
      completion_function tmp(funct_);
      funct_ = lslboost::move(funct);
      return tmp;
    }
#endif
    completion_function then(void(*funct)())
    {
      lslboost::lock_guard<lslboost::mutex> lk(mutex_);
      completion_function tmp(funct_);
      funct_ = completion_function(funct);
      return tmp;
    }

  private:
    mutex mutex_;
    detail::counter count_;
    completion_function funct_;
    detail::counter waiters_;
    detail::counter leavers_;
  };

} // namespace lslboost

#include <lslboost/config/abi_suffix.hpp>

#endif
