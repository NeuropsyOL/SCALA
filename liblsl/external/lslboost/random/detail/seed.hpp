/* lslboost random/detail/seed.hpp header file
 *
 * Copyright Steven Watanabe 2009
 * Distributed under the Boost Software License, Version 1.0. (See
 * accompanying file LICENSE_1_0.txt or copy at
 * http://www.lslboost.org/LICENSE_1_0.txt)
 *
 * See http://www.lslboost.org for most recent version including documentation.
 *
 * $Id: seed.hpp 71018 2011-04-05 21:27:52Z steven_watanabe $
 */

#ifndef BOOST_RANDOM_DETAIL_SEED_HPP
#define BOOST_RANDOM_DETAIL_SEED_HPP

#include <lslboost/config.hpp>

// Sun seems to have trouble with the use of SFINAE for the
// templated constructor.  So does Borland.
#if !defined(BOOST_NO_SFINAE) && !defined(__SUNPRO_CC) && !defined(__BORLANDC__)

#include <lslboost/utility/enable_if.hpp>
#include <lslboost/type_traits/is_arithmetic.hpp>

namespace lslboost {
namespace random {
namespace detail {

template<class T>
struct disable_seed : lslboost::disable_if<lslboost::is_arithmetic<T> > {};

template<class Engine, class T>
struct disable_constructor : disable_seed<T> {};

template<class Engine>
struct disable_constructor<Engine, Engine> {};

#define BOOST_RANDOM_DETAIL_GENERATOR_CONSTRUCTOR(Self, Generator, gen) \
    template<class Generator>                                           \
    explicit Self(Generator& gen, typename ::lslboost::random::detail::disable_constructor<Self, Generator>::type* = 0)

#define BOOST_RANDOM_DETAIL_GENERATOR_SEED(Self, Generator, gen)    \
    template<class Generator>                                       \
    void seed(Generator& gen, typename ::lslboost::random::detail::disable_seed<Generator>::type* = 0)

#define BOOST_RANDOM_DETAIL_SEED_SEQ_CONSTRUCTOR(Self, SeedSeq, seq)    \
    template<class SeedSeq>                                             \
    explicit Self(SeedSeq& seq, typename ::lslboost::random::detail::disable_constructor<Self, SeedSeq>::type* = 0)

#define BOOST_RANDOM_DETAIL_SEED_SEQ_SEED(Self, SeedSeq, seq)   \
    template<class SeedSeq>                                     \
    void seed(SeedSeq& seq, typename ::lslboost::random::detail::disable_seed<SeedSeq>::type* = 0)

#define BOOST_RANDOM_DETAIL_ARITHMETIC_CONSTRUCTOR(Self, T, x)  \
    explicit Self(const T& x)

#define BOOST_RANDOM_DETAIL_ARITHMETIC_SEED(Self, T, x) \
    void seed(const T& x)
}
}
}

#else

#include <lslboost/type_traits/is_arithmetic.hpp>
#include <lslboost/mpl/bool.hpp>

#define BOOST_RANDOM_DETAIL_GENERATOR_CONSTRUCTOR(Self, Generator, gen) \
    Self(Self& other) { *this = other; }                                \
    Self(const Self& other) { *this = other; }                          \
    template<class Generator>                                           \
    explicit Self(Generator& gen) {                                     \
        lslboost_random_constructor_impl(gen, ::lslboost::is_arithmetic<Generator>());\
    }                                                                   \
    template<class Generator>                                           \
    void lslboost_random_constructor_impl(Generator& gen, ::lslboost::mpl::false_)

#define BOOST_RANDOM_DETAIL_GENERATOR_SEED(Self, Generator, gen)    \
    template<class Generator>                                       \
    void seed(Generator& gen) {                                     \
        lslboost_random_seed_impl(gen, ::lslboost::is_arithmetic<Generator>());\
    }\
    template<class Generator>\
    void lslboost_random_seed_impl(Generator& gen, ::lslboost::mpl::false_)

#define BOOST_RANDOM_DETAIL_SEED_SEQ_CONSTRUCTOR(Self, SeedSeq, seq)    \
    Self(Self& other) { *this = other; }                                \
    Self(const Self& other) { *this = other; }                          \
    template<class SeedSeq>                                             \
    explicit Self(SeedSeq& seq) {                                       \
        lslboost_random_constructor_impl(seq, ::lslboost::is_arithmetic<SeedSeq>());\
    }                                                                   \
    template<class SeedSeq>                                             \
    void lslboost_random_constructor_impl(SeedSeq& seq, ::lslboost::mpl::false_)

#define BOOST_RANDOM_DETAIL_SEED_SEQ_SEED(Self, SeedSeq, seq)           \
    template<class SeedSeq>                                             \
    void seed(SeedSeq& seq) {                                           \
        lslboost_random_seed_impl(seq, ::lslboost::is_arithmetic<SeedSeq>()); \
    }                                                                   \
    template<class SeedSeq>                                             \
    void lslboost_random_seed_impl(SeedSeq& seq, ::lslboost::mpl::false_)

#define BOOST_RANDOM_DETAIL_ARITHMETIC_CONSTRUCTOR(Self, T, x)  \
    explicit Self(const T& x) { lslboost_random_constructor_impl(x, ::lslboost::mpl::true_()); }\
    void lslboost_random_constructor_impl(const T& x, ::lslboost::mpl::true_)

#define BOOST_RANDOM_DETAIL_ARITHMETIC_SEED(Self, T, x) \
    void seed(const T& x) { lslboost_random_seed_impl(x, ::lslboost::mpl::true_()); }\
    void lslboost_random_seed_impl(const T& x, ::lslboost::mpl::true_)

#endif

#endif
