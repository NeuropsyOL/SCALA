#ifndef BOOST_ARCHIVE_BASIC_BINARY_OPRIMITIVE_HPP
#define BOOST_ARCHIVE_BASIC_BINARY_OPRIMITIVE_HPP

// MS compatible compilers support #pragma once
#if defined(_MSC_VER) && (_MSC_VER >= 1020)
# pragma once
#endif

/////////1/////////2/////////3/////////4/////////5/////////6/////////7/////////8
// basic_binary_oprimitive.hpp

// (C) Copyright 2002 Robert Ramey - http://www.rrsd.com . 
// Use, modification and distribution is subject to the Boost Software
// License, Version 1.0. (See accompanying file LICENSE_1_0.txt or copy at
// http://www.lslboost.org/LICENSE_1_0.txt)

//  See http://www.lslboost.org for updates, documentation, and revision history.

// archives stored as native binary - this should be the fastest way
// to archive the state of a group of obects.  It makes no attempt to
// convert to any canonical form.

// IN GENERAL, ARCHIVES CREATED WITH THIS CLASS WILL NOT BE READABLE
// ON PLATFORM APART FROM THE ONE THEY ARE CREATE ON

#include <iosfwd>
#include <lslboost/assert.hpp>
#include <locale>
#include <streambuf> // basic_streambuf
#include <string>
#include <cstddef> // size_t

#include <lslboost/config.hpp>
#if defined(BOOST_NO_STDC_NAMESPACE)
namespace std{ 
    using ::size_t; 
} // namespace std
#endif

#include <lslboost/cstdint.hpp>
#include <lslboost/integer.hpp>
#include <lslboost/integer_traits.hpp>
#include <lslboost/scoped_ptr.hpp>
#include <lslboost/serialization/throw_exception.hpp>

#include <lslboost/archive/basic_streambuf_locale_saver.hpp>
#include <lslboost/archive/archive_exception.hpp>
#include <lslboost/serialization/is_bitwise_serializable.hpp>
#include <lslboost/mpl/placeholders.hpp>
#include <lslboost/serialization/array.hpp>
#include <lslboost/archive/detail/auto_link_archive.hpp>
#include <lslboost/archive/detail/abi_prefix.hpp> // must be the last header

namespace lslboost {
namespace archive {

/////////////////////////////////////////////////////////////////////////
// class basic_binary_oprimitive - binary output of prmitives

template<class Archive, class Elem, class Tr>
class basic_binary_oprimitive
{
#ifndef BOOST_NO_MEMBER_TEMPLATE_FRIENDS
    friend class save_access;
protected:
#else
public:
#endif
    std::basic_streambuf<Elem, Tr> & m_sb;
    // return a pointer to the most derived class
    Archive * This(){
        return static_cast<Archive *>(this);
    }
    #ifndef BOOST_NO_STD_LOCALE
    lslboost::scoped_ptr<std::locale> archive_locale;
    basic_streambuf_locale_saver<Elem, Tr> locale_saver;
    #endif
    // default saving of primitives.
    template<class T>
    void save(const T & t)
    {
        save_binary(& t, sizeof(T));
    }

    /////////////////////////////////////////////////////////
    // fundamental types that need special treatment
    
    // trap usage of invalid uninitialized boolean which would
    // otherwise crash on load.
    void save(const bool t){
        BOOST_ASSERT(0 == static_cast<int>(t) || 1 == static_cast<int>(t));
        save_binary(& t, sizeof(t));
    }
    BOOST_ARCHIVE_OR_WARCHIVE_DECL(void)
    save(const std::string &s);
    #ifndef BOOST_NO_STD_WSTRING
    BOOST_ARCHIVE_OR_WARCHIVE_DECL(void)
    save(const std::wstring &ws);
    #endif
    BOOST_ARCHIVE_OR_WARCHIVE_DECL(void)
    save(const char * t);
    BOOST_ARCHIVE_OR_WARCHIVE_DECL(void)
    save(const wchar_t * t);

    BOOST_ARCHIVE_OR_WARCHIVE_DECL(void)
    init();
    
    BOOST_ARCHIVE_OR_WARCHIVE_DECL(BOOST_PP_EMPTY()) 
    basic_binary_oprimitive(
        std::basic_streambuf<Elem, Tr> & sb, 
        bool no_codecvt
    );
    BOOST_ARCHIVE_OR_WARCHIVE_DECL(BOOST_PP_EMPTY()) 
    ~basic_binary_oprimitive();
public:

    // we provide an optimized save for all fundamental types
    // typedef serialization::is_bitwise_serializable<mpl::_1> 
    // use_array_optimization;
    // workaround without using mpl lambdas
    struct use_array_optimization {
        template <class T>  
        #if defined(BOOST_NO_DEPENDENT_NESTED_DERIVATIONS)  
            struct apply {  
                typedef BOOST_DEDUCED_TYPENAME lslboost::serialization::is_bitwise_serializable< T >::type type;  
            };
        #else
            struct apply : public lslboost::serialization::is_bitwise_serializable< T > {};  
        #endif
    };
    

    // the optimized save_array dispatches to save_binary 
    template <class ValueType>
    void save_array(lslboost::serialization::array<ValueType> const& a, unsigned int)
    {
      save_binary(a.address(),a.count()*sizeof(ValueType));
    }

    void save_binary(const void *address, std::size_t count);
};

template<class Archive, class Elem, class Tr>
inline void 
basic_binary_oprimitive<Archive, Elem, Tr>::save_binary(
    const void *address, 
    std::size_t count
){
    //BOOST_ASSERT(
    //    static_cast<std::size_t>((std::numeric_limits<std::streamsize>::max)()) >= count
    //);
    // note: if the following assertions fail
    // a likely cause is that the output stream is set to "text"
    // mode where by cr characters recieve special treatment.
    // be sure that the output stream is opened with ios::binary
    //if(os.fail())
    //    lslboost::serialization::throw_exception(
    //        archive_exception(archive_exception::output_stream_error)
    //    );
    // figure number of elements to output - round up
    count = ( count + sizeof(Elem) - 1) 
        / sizeof(Elem);
    BOOST_ASSERT(count <= std::size_t(lslboost::integer_traits<std::streamsize>::const_max));
    std::streamsize scount = m_sb.sputn(
        static_cast<const Elem *>(address), 
        static_cast<std::streamsize>(count)
    );
    if(count != static_cast<std::size_t>(scount))
        lslboost::serialization::throw_exception(
            archive_exception(archive_exception::output_stream_error)
        );
    //os.write(
    //    static_cast<const BOOST_DEDUCED_TYPENAME OStream::char_type *>(address), 
    //    count
    //);
    //BOOST_ASSERT(os.good());
}

} //namespace lslboost 
} //namespace archive 

#include <lslboost/archive/detail/abi_suffix.hpp> // pop pragmas

#endif // BOOST_ARCHIVE_BASIC_BINARY_OPRIMITIVE_HPP
