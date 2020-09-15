#ifndef BOOST_ARCHIVE_BINARY_WIARCHIVE_HPP
#define BOOST_ARCHIVE_BINARY_WIARCHIVE_HPP

// MS compatible compilers support #pragma once
#if defined(_MSC_VER) && (_MSC_VER >= 1020)
# pragma once
#endif

/////////1/////////2/////////3/////////4/////////5/////////6/////////7/////////8
// binary_wiarchive.hpp

// (C) Copyright 2002 Robert Ramey - http://www.rrsd.com . 
// Use, modification and distribution is subject to the Boost Software
// License, Version 1.0. (See accompanying file LICENSE_1_0.txt or copy at
// http://www.lslboost.org/LICENSE_1_0.txt)

//  See http://www.lslboost.org for updates, documentation, and revision history.

#include <lslboost/config.hpp>
#ifdef BOOST_NO_STD_WSTREAMBUF
#error "wide char i/o not supported on this platform"
#else

#include <istream> // wistream
#include <lslboost/archive/binary_iarchive_impl.hpp>
#include <lslboost/archive/detail/register_archive.hpp>

namespace lslboost { 
namespace archive {

// same as binary_wiarchive below - without the shared_ptr_helper
class naked_binary_wiarchive : 
    public binary_iarchive_impl<
        lslboost::archive::naked_binary_wiarchive, 
        std::wistream::char_type, 
        std::wistream::traits_type
    >
{
public:
    naked_binary_wiarchive(std::wistream & is, unsigned int flags = 0) :
        binary_iarchive_impl<
            naked_binary_wiarchive, 
            std::wistream::char_type, 
            std::wistream::traits_type
        >(is, flags)
    {}
    naked_binary_wiarchive(std::wstreambuf & bsb, unsigned int flags = 0) :
        binary_iarchive_impl<
            naked_binary_wiarchive, 
            std::wistream::char_type, 
            std::wistream::traits_type
        >(bsb, flags)
    {}
};

} // namespace archive
} // namespace lslboost

// note special treatment of shared_ptr. This type needs a special
// structure associated with every archive.  We created a "mix-in"
// class to provide this functionality.  Since shared_ptr holds a
// special esteem in the lslboost library - we included it here by default.
#include <lslboost/archive/shared_ptr_helper.hpp>

namespace lslboost { 
namespace archive {

class binary_wiarchive : 
    public binary_iarchive_impl<
        binary_wiarchive, std::wistream::char_type, std::wistream::traits_type
    >
{
public:
    binary_wiarchive(std::wistream & is, unsigned int flags = 0) :
        binary_iarchive_impl<
            binary_wiarchive, std::wistream::char_type, std::wistream::traits_type
        >(is, flags)
    {}
    binary_wiarchive(std::wstreambuf & bsb, unsigned int flags = 0) :
        binary_iarchive_impl<
            binary_wiarchive, std::wistream::char_type, std::wistream::traits_type
        >(bsb, flags)
    {}
};

} // namespace archive
} // namespace lslboost

// required by export
BOOST_SERIALIZATION_REGISTER_ARCHIVE(lslboost::archive::binary_wiarchive)

#endif // BOOST_NO_STD_WSTREAMBUF
#endif // BOOST_ARCHIVE_BINARY_WIARCHIVE_HPP
