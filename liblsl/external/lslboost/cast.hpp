//  lslboost cast.hpp header file  ----------------------------------------------//

//  (C) Copyright Kevlin Henney and Dave Abrahams 1999.
//  Distributed under the Boost
//  Software License, Version 1.0. (See accompanying file
//  LICENSE_1_0.txt or copy at http://www.lslboost.org/LICENSE_1_0.txt)

//  See http://www.lslboost.org/libs/conversion for Documentation.

//  Revision History
//  23 JUn 05  numeric_cast removed and redirected to the new verion (Fernando Cacciola)
//  02 Apr 01  Removed BOOST_NO_LIMITS workarounds and included
//             <lslboost/limits.hpp> instead (the workaround did not
//             actually compile when BOOST_NO_LIMITS was defined in
//             any case, so we loose nothing). (John Maddock)
//  21 Jan 01  Undid a bug I introduced yesterday. numeric_cast<> never
//             worked with stock GCC; trying to get it to do that broke
//             vc-stlport.
//  20 Jan 01  Moved BOOST_NO_LIMITS_COMPILE_TIME_CONSTANTS to config.hpp.
//             Removed unused BOOST_EXPLICIT_TARGET macro. Moved
//             lslboost::detail::type to lslboost/type.hpp. Made it compile with
//             stock gcc again (Dave Abrahams)
//  29 Nov 00  Remove nested namespace cast, cleanup spacing before Formal
//             Review (Beman Dawes)
//  19 Oct 00  Fix numeric_cast for floating-point types (Dave Abrahams)
//  15 Jul 00  Suppress numeric_cast warnings for GCC, Borland and MSVC
//             (Dave Abrahams)
//  30 Jun 00  More MSVC6 wordarounds.  See comments below.  (Dave Abrahams)
//  28 Jun 00  Removed implicit_cast<>.  See comment below. (Beman Dawes)
//  27 Jun 00  More MSVC6 workarounds
//  15 Jun 00  Add workarounds for MSVC6
//   2 Feb 00  Remove bad_numeric_cast ";" syntax error (Doncho Angelov)
//  26 Jan 00  Add missing throw() to bad_numeric_cast::what(0 (Adam Levar)
//  29 Dec 99  Change using declarations so usages in other namespaces work
//             correctly (Dave Abrahams)
//  23 Sep 99  Change polymorphic_downcast assert to also detect M.I. errors
//             as suggested Darin Adler and improved by Valentin Bonnard.
//   2 Sep 99  Remove controversial asserts, simplify, rename.
//  30 Aug 99  Move to cast.hpp, replace value_cast with numeric_cast,
//             place in nested namespace.
//   3 Aug 99  Initial version

#ifndef BOOST_CAST_HPP
#define BOOST_CAST_HPP

# include <lslboost/config.hpp>
# include <lslboost/assert.hpp>
# include <typeinfo>
# include <lslboost/type.hpp>
# include <lslboost/limits.hpp>
# include <lslboost/detail/select_type.hpp>

//  It has been demonstrated numerous times that MSVC 6.0 fails silently at link
//  time if you use a template function which has template parameters that don't
//  appear in the function's argument list.
//
//  TODO: Add this to config.hpp?
# if defined(BOOST_MSVC) && BOOST_MSVC < 1300
#  define BOOST_EXPLICIT_DEFAULT_TARGET , ::lslboost::type<Target>* = 0
# else
#  define BOOST_EXPLICIT_DEFAULT_TARGET
# endif

namespace lslboost
{
//  See the documentation for descriptions of how to choose between
//  static_cast<>, dynamic_cast<>, polymorphic_cast<> and polymorphic_downcast<>

//  polymorphic_cast  --------------------------------------------------------//

    //  Runtime checked polymorphic downcasts and crosscasts.
    //  Suggested in The C++ Programming Language, 3rd Ed, Bjarne Stroustrup,
    //  section 15.8 exercise 1, page 425.

    template <class Target, class Source>
    inline Target polymorphic_cast(Source* x BOOST_EXPLICIT_DEFAULT_TARGET)
    {
        Target tmp = dynamic_cast<Target>(x);
        if ( tmp == 0 ) throw std::bad_cast();
        return tmp;
    }

//  polymorphic_downcast  ----------------------------------------------------//

    //  BOOST_ASSERT() checked polymorphic downcast.  Crosscasts prohibited.

    //  WARNING: Because this cast uses BOOST_ASSERT(), it violates
    //  the One Definition Rule if used in multiple translation units
    //  where BOOST_DISABLE_ASSERTS, BOOST_ENABLE_ASSERT_HANDLER
    //  NDEBUG are defined inconsistently.

    //  Contributed by Dave Abrahams

    template <class Target, class Source>
    inline Target polymorphic_downcast(Source* x BOOST_EXPLICIT_DEFAULT_TARGET)
    {
        BOOST_ASSERT( dynamic_cast<Target>(x) == x );  // detect logic error
        return static_cast<Target>(x);
    }

#  undef BOOST_EXPLICIT_DEFAULT_TARGET

} // namespace lslboost

# include <lslboost/numeric/conversion/cast.hpp>

#endif  // BOOST_CAST_HPP
