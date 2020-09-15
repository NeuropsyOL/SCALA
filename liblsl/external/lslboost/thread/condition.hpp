#ifndef BOOST_THREAD_CONDITION_HPP
#define BOOST_THREAD_CONDITION_HPP
//  (C) Copyright 2007 Anthony Williams
//
//  Distributed under the Boost Software License, Version 1.0. (See
//  accompanying file LICENSE_1_0.txt or copy at
//  http://www.lslboost.org/LICENSE_1_0.txt)

#include <lslboost/thread/detail/config.hpp>

#if defined BOOST_THREAD_PROVIDES_CONDITION

#include <lslboost/thread/condition_variable.hpp>

namespace lslboost
{
    typedef condition_variable_any condition;
}

#endif
#endif
