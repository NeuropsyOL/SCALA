#ifndef BOOST_ARCHIVE_BINARY_IARCHIVE_HPP
#define BOOST_ARCHIVE_BINARY_IARCHIVE_HPP

// MS compatible compilers support #pragma once
#if defined(_MSC_VER) && (_MSC_VER >= 1020)
# pragma once
#endif

/////////1/////////2/////////3/////////4/////////5/////////6/////////7/////////8
// binary_iarchive.hpp

// (C) Copyright 2002 Robert Ramey - http://www.rrsd.com . 
// Use, modification and distribution is subject to the Boost Software
// License, Version 1.0. (See accompanying file LICENSE_1_0.txt or copy at
// http://www.lslboost.org/LICENSE_1_0.txt)

//  See http://www.lslboost.org for updates, documentation, and revision history.

#include <istream>
#include <lslboost/archive/binary_iarchive_impl.hpp>
#include <lslboost/archive/detail/register_archive.hpp>

#ifdef BOOST_MSVC
#  pragma warning(push)
#  pragma warning(disable : 4511 4512)
#endif

namespace lslboost { 
namespace archive {

// do not derive from the classes below.  If you want to extend this functionality
// via inhertance, derived from text_iarchive_impl instead.  This will
// preserve correct static polymorphism.

// same as binary_iarchive below - without the shared_ptr_helper
class naked_binary_iarchive : 
    public binary_iarchive_impl<
        lslboost::archive::naked_binary_iarchive, 
        std::istream::char_type, 
        std::istream::traits_type
    >
{
public:
    naked_binary_iarchive(std::istream & is, unsigned int flags = 0) :
        binary_iarchive_impl<
            naked_binary_iarchive, std::istream::char_type, std::istream::traits_type
        >(is, flags)
    {}
    naked_binary_iarchive(std::streambuf & bsb, unsigned int flags = 0) :
        binary_iarchive_impl<
            naked_binary_iarchive, std::istream::char_type, std::istream::traits_type
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

// do not derive from this class.  If you want to extend this functionality
// via inhertance, derived from binary_iarchive_impl instead.  This will
// preserve correct static polymorphism.
class binary_iarchive : 
    public binary_iarchive_impl<
        lslboost::archive::binary_iarchive, 
        std::istream::char_type, 
        std::istream::traits_type
    >,
    public detail::shared_ptr_helper
{
public:
    binary_iarchive(std::istream & is, unsigned int flags = 0) :
        binary_iarchive_impl<
            binary_iarchive, std::istream::char_type, std::istream::traits_type
        >(is, flags)
    {}
    binary_iarchive(std::streambuf & bsb, unsigned int flags = 0) :
        binary_iarchive_impl<
            binary_iarchive, std::istream::char_type, std::istream::traits_type
        >(bsb, flags)
    {}
};

} // namespace archive
} // namespace lslboost

// required by export
BOOST_SERIALIZATION_REGISTER_ARCHIVE(lslboost::archive::binary_iarchive)
BOOST_SERIALIZATION_USE_ARRAY_OPTIMIZATION(lslboost::archive::binary_iarchive)

#ifdef BOOST_MSVC
#pragma warning(pop)
#endif

#endif // BOOST_ARCHIVE_BINARY_IARCHIVE_HPP
